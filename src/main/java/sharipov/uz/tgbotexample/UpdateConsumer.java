package sharipov.uz.tgbotexample;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient(
                "8106627774:AAFCG90E_NJ-4jVbHwIOIRtjfXojIxx6SPU");
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (text.equals("/start")) {

                sendMainMenu(chatId);
            } else if(text.equals("/keyboard"))  {
                sendReplyKeyBoard(chatId);

            }
            else if(text.equals("Contact"))  {
                sendMyName(chatId,update.getMessage().getFrom());
            }
            else if(text.equals("Image"))  {
               sendImage(chatId);

            }else {
                sendMessage(chatId, "Men sizni tushunmadim?");
            }

        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update.getCallbackQuery());
        }
    }

    @SneakyThrows
    private void sendReplyKeyBoard(Long chatId) {
        SendMessage message = SendMessage.builder()
                .text("Keyboard...")
                .chatId(chatId)
                .build();

        List<KeyboardRow> keyboardRows = List.of(
                new KeyboardRow("Contact","Image"));

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);


        telegramClient.execute(message);

    }

    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var user = callbackQuery.getFrom();

        switch (data) {
            case "name" -> sendMyName(chatId, user);
            case "data" -> sendData(chatId);
            case "etc" -> sendImage(chatId);
            default -> sendMessage(chatId, "Error");
        }
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();
        telegramClient.execute(message);

    }

    private void sendImage(Long chatId) {
        sendMessage(chatId, "Downloading image...");
        new Thread(() -> {
            var imageUrl = "https://picsum.photos/200";
            try {
                URL url =  new URL(imageUrl);
                var inputStream  = url.openStream();

                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(inputStream,"random.jpg"))
                        .caption("Random image: ")
                        .build();

                telegramClient.execute(sendPhoto)
;            } catch (TelegramApiException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();

    }

    private void sendData(Long chatId) {

        var randomInt = ThreadLocalRandom.current().nextInt();
        sendMessage(chatId, "Random son "+ randomInt);

    }

    private void sendMyName(Long chatId, User user) {

        var text = "Salom! \n\n Sizning isminngiz: %s\n Sizning username: @%s"
                .formatted(
                        user.getFirstName() + " " + user.getLastName(),
                        user.getUserName()
                );
        sendMessage(chatId, text);
    }


    @SneakyThrows
    private void sendMainMenu(Long chatId) {

        SendMessage message = SendMessage.builder()
                .text("Welcome to TgBotExample!")
                .chatId(chatId)
                .build();

        var button1 = InlineKeyboardButton.builder()
                .text("What is your name?")
                .callbackData("name")
                .build();
        var button2 = InlineKeyboardButton.builder()
                .text("Random number")
                .callbackData("data")
                .build();

        var button3 = InlineKeyboardButton.builder()
                .text("Random photo...")
                .callbackData("etc")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }
}
