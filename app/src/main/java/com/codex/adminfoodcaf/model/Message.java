package com.codex.adminfoodcaf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String messageId;
    private String senderId;     // මැසේජ් එක යවන කෙනාගේ ID එක
    private String receiverId;   // මැසේජ් එක ලබන කෙනාගේ ID එක (Admin ගේ ID එක)
    private String messageText;
    private long timestamp;      // මැසේජ් එක යවපු වෙලාව
}