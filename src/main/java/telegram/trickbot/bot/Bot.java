package telegram.trickbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import telegram.trickbot.service.DeckManager;
import telegram.trickbot.service.MessagesManager;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {
    private final DeckManager deckManager;
    private final MessagesManager messageSender;
    private int step = 0;
    private long chatId;
    private int messageId;
    private String userName;

    @Value("${bot.username}")
    private String botUsername;

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
        this.deckManager = new DeckManager();
        this.messageSender = new MessagesManager(this);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            userName = update.getMessage().getFrom().getFirstName();
            log.info("chat ID: {}, user name: {}", chatId, userName);

            if (update.getMessage().getText().equalsIgnoreCase("/start")) {
                messageSender.sendWelcomeMessage(chatId, userName);
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            log.info("chat ID: {}, callback data: {}", chatId, callbackData);

            if (callbackData.equals("start_trick")) {
                startTrick(chatId);
            }
            else {
                handleButtonPress(callbackData);
            }
        }
    }

    private void startTrick(long chatId) {
        this.chatId = chatId;
        deckManager.shuffleDeck();
        step = 0;
        performTrick();
    }

    private void performTrick() {
        messageSender.sendTextMessage(chatId, "👇 СТОПКА 1 👇");
        messageSender.sendMediaGroup(chatId, deckManager.getPile1());

        messageSender.sendTextMessage(chatId, "👇 СТОПКА 2 👇");
        messageSender.sendMediaGroup(chatId, deckManager.getPile2());

        messageSender.sendTextMessage(chatId, "👇 СТОПКА 3 👇");
        messageSender.sendMediaGroup(chatId, deckManager.getPile3());

        InlineKeyboardMarkup keyboardMarkup = messageSender.createInlineKeyboard();
        Message message = messageSender.sendMessageWithKeyboard(
                chatId, "В какой стопке компания, которую вы загадали?", keyboardMarkup);
        messageId = message.getMessageId();
    }

    private void handleButtonPress(String data) {
        int chosenPile = Integer.parseInt(data);
        deckManager.updateDeck(chosenPile);
        step++;
        log.info("Chosen pile: {}, Step: {}", chosenPile, step);

        if (step < 3) {
            performTrick();
        }
        else {
            showEleventhCard();
            messageSender.sendWelcomeMessage(chatId, userName);
        }
    }

    private void showEleventhCard() {
        String cardToShow = deckManager.getSelectedCard();
        log.info("Card to show: {}", cardToShow);
        messageSender.sendPhoto(chatId, cardToShow,"Компания, которую вы загадали\uD83D\uDC46");
    }

}