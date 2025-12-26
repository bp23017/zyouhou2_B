package com.example.application.Client.Entity;

import org.springframework.stereotype.Service;

@Service
public class ClienttoApplication {

    /**
     * 画面（ClientManager）から呼び出されるメソッド
     * Lombokのクラス定義に合わせてコンストラクタでデータをセットします
     * * @param senderName 送信者の名前
     * @param type       メッセージの種類（例: "DICE", "JOIN"） -> taskNameに入れます
     * @param content    具体的な内容（例: "5", "参加希望"） -> passwordに入れます(※)
     */
    public void sendMessage(String senderName, String type, String content) {
        
        // Lombokの @AllArgsConstructor を使ってインスタンス化
        // 引数の順番: taskName, userId, userName, password
        ClientToClientManagementMessage message = new ClientToClientManagementMessage(
            type,       // taskName
            senderName, // userId
            senderName, // userName
            content     // password (本来はパスワード用ですが、汎用データとしてここを使います)
        );

        // 送信処理
        sendToNetwork(message);
    }

    /**
     * 実際の通信処理（シミュレーション）
     */
    private void sendToNetwork(ClientToClientManagementMessage msg) {
        System.out.println("--- [Client -> Server] 送信 (Lombok版) ---");
        System.out.println("TaskName: " + msg.getTaskName()); // @Getterがあるのでgetできる
        System.out.println("UserName: " + msg.getUserName());
        System.out.println("Data:     " + msg.getPassword()); // 中身はcontent
        System.out.println("----------------------------------------");
        
        // TODO: ここに template.convertAndSend(...) 等が入ります
    }
}