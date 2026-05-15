package com.stu.helloserver.model.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.stu.helloserver.model.dto.ChatRequestDTO;
import com.stu.helloserver.model.service.ChatService;
import com.stu.helloserver.model.vo.ChatResponseVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final StringRedisTemplate stringRedisTemplate;

    // Redis Key 前缀
    private static final String SESSION_KEY_PREFIX = "chat:session:";
    // 会话过期时间 1 小时
    private static final long SESSION_TTL = 3600;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           StringRedisTemplate stringRedisTemplate) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一名专业、友好、简洁的中文智能助手，请结合历史对话回答用户问题。")
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String sessionId = requestDTO.getSessionId();
        String message = requestDTO.getMessage();
        String redisKey = SESSION_KEY_PREFIX + sessionId;

        // 1. 读取历史消息
        List<String> records = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        String historyText = "";
        if (records != null && !records.isEmpty()) {
            historyText = String.join("\n", records);
        }

        // 2. 拼接上下文
        String finalPrompt = String.format("""
                以下是历史对话：
                %s

                当前用户问题：
                %s
                """, historyText, message);

        // 3. 调用模型
        String answer = chatClient.prompt(finalPrompt)
                .call()
                .content();

        // 4. 保存本轮记录
        String recordText = "用户：" + message + "\n助手：" + answer;
        stringRedisTemplate.opsForList().rightPush(redisKey, recordText);

        // 5. 只保留最近 3 轮对话
        Long size = stringRedisTemplate.opsForList().size(redisKey);
        if (size != null && size > 3) {
            stringRedisTemplate.opsForList().trim(redisKey, size - 3, size - 1);
        }

        // 设置会话过期时间
        stringRedisTemplate.expire(redisKey, SESSION_TTL, TimeUnit.SECONDS);

        return new ChatResponseVO(message, answer);
    }
}