package com.kunting.client.chat;

import com.kunting.bean.Message;
import com.kunting.bean.User;
import com.kunting.client.util.Drag;
import com.kunting.client.util.ThreadPoolUtil;
import com.trynotifications.animations.AnimationType;
import com.trynotifications.bean.BubbleSpec;
import com.trynotifications.bean.BubbledLabel;
import com.trynotifications.notification.TrayNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    @FXML private TextArea messageBox;
    @FXML private Label usernameLabel;// display username
    @FXML private Label onlineCountLabel;//display total user online
    @FXML private ListView userList;// user list
    @FXML private ImageView userImageView;// profile
    @FXML ListView chatPane;
    @FXML BorderPane borderPane;

    /**
     * handle sendButton action
     * @throws IOException
     */
    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();

        if (!msg.isEmpty()) {
            Listener.instance.send(msg);

            // clear out the text box when the user sent the text
            messageBox.clear();
        }
    }


    /**
     *  display sent message
     * @param msg
     */
    public synchronized void showMsg(Message msg) {

        //别人发送的信息
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() {
                Image image = new Image(getClass().getClassLoader().getResource("images/" + msg.getPicture().toLowerCase() + ".png").toString());
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);
                BubbledLabel bl = new BubbledLabel();
                bl.setText(msg.getName() + ": " + msg.getMsg());
                bl.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));
                HBox x = new HBox();
                bl.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                x.getChildren().addAll(profileImage, bl);
                setOnlineLabel(String.valueOf(msg.getOnlineUsers().size()));
                return x;
            }
        };

        othersMessages.setOnSucceeded(event ->
            chatPane.getItems().add(othersMessages.getValue())
        );

        // message sent by user
        Task<HBox> yourMessages = new Task<HBox>() {
            @Override
            public HBox call() {
                Image image = userImageView.getImage();
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);

                BubbledLabel bl = new BubbledLabel();
                bl.setText(msg.getMsg());
                bl.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                        null, null)));
                HBox x = new HBox();
                x.setMaxWidth(chatPane.getWidth() - 20);
                x.setAlignment(Pos.TOP_RIGHT);
                bl.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
                x.getChildren().addAll(bl, profileImage);

                setOnlineLabel(String.valueOf(msg.getOnlineUsers().size()));
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> chatPane.getItems().add(yourMessages.getValue()));


        if (msg.getName().equals(usernameLabel.getText())) {
            ThreadPoolUtil.poolExecutor.execute(yourMessages);
        } else {
            ThreadPoolUtil.poolExecutor.execute(othersMessages);
        }

    }

    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }

    public void setOnlineLabel(String count) {
        Platform.runLater(() -> onlineCountLabel.setText(count));
    }

    public void setUserList(Message msg) {
        Platform.runLater(() -> {
            ObservableList<User> users = FXCollections.observableList(msg.getOnlineUsers());
            userList.setItems(users);
            userList.setCellFactory(new CellRenderer());
            setOnlineLabel(String.valueOf(msg.getOnlineUsers().size()));
        });
    }


    /**
     *  user notify
     * @param msg
     * @param picture
     * @param title
     * @param sound
     */
    public void notify(String msg,String picture,String title,String sound) {
        Platform.runLater(() -> {
            Image profileImg = new Image(getClass().getClassLoader().getResource("images/" + picture +".png").toString(),50,50,false,false);
            TrayNotification tray = new TrayNotification();
            tray.setTitle(title);
            tray.setMessage(msg);
            tray.setRectangleFill(Paint.valueOf("#2C3E50"));
            tray.setAnimationType(AnimationType.POPUP);
            tray.setImage(profileImg);
            tray.showAndDismiss(Duration.seconds(5));
            try {
                Media hit = new Media(getClass().getClassLoader().getResource(sound).toString());
                MediaPlayer mediaPlayer = new MediaPlayer(hit);
                mediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void sendMethod(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            sendButtonAction();
        }
    }

    @FXML
    public void closeApplication() {
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //处理鼠标拖拽界面
        new Drag().handleDrag(borderPane);

        //处理按下回车事件
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });

    }

    public void setImageLabel(String selectedPicture) {
        String path = "";

        switch (selectedPicture) {
            case "boy":
                path = "images/boy.png";
                break;
            case "girl":
                path = "images/girl.png";
                break;
            case "default":
                path = "images/default.png";
                break;
        }

        this.userImageView.setImage(new Image(getClass().getClassLoader().getResource(path).toString()));
    }

}