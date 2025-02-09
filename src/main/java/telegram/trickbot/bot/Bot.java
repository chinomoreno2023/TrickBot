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
            messageSender.addMessageId(update.getMessage().getMessageId());
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getFirstName();
            log.info("chat ID: {}, user name: {}", chatId, userName);

            if (update.getMessage().getText().equalsIgnoreCase("/start")) {
                messageSender.deleteMessages(chatId);
                messageSender.sendWelcomeMessage(chatId, userName);
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            log.info("chat ID: {}, callback data: {}", chatId, callbackData);

            if (callbackData.equals("start_trick")) {
                messageSender.deleteMessages(chatId);
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
        messageSender.addMessageId(messageSender.sendTextMessage(chatId, "👇 СТОПКА 1 👇").getMessageId());
        List<Message> pile1Messages = messageSender.sendMediaGroup(chatId, deckManager.getPile1());
        messageSender.addMessageId(pile1Messages.stream().map(Message::getMessageId).toList());

        messageSender.addMessageId(messageSender.sendTextMessage(chatId, "👇 СТОПКА 2 👇").getMessageId());
        List<Message> pile2Messages = messageSender.sendMediaGroup(chatId, deckManager.getPile2());
        messageSender.addMessageId(pile2Messages.stream().map(Message::getMessageId).toList());

        messageSender.addMessageId(messageSender.sendTextMessage(chatId, "👇 СТОПКА 3 👇").getMessageId());
        List<Message> pile3Messages = messageSender.sendMediaGroup(chatId, deckManager.getPile3());
        messageSender.addMessageId(pile3Messages.stream().map(Message::getMessageId).toList());

        InlineKeyboardMarkup keyboardMarkup = messageSender.createInlineKeyboard();
        Message message = messageSender.sendMessageWithKeyboard(
                chatId, "В какой стопке компания, которую вы загадали?", keyboardMarkup);
        messageSender.addMessageId(message.getMessageId());
        messageId = message.getMessageId();
    }

    private void handleButtonPress(String data) {
        int chosenPile = Integer.parseInt(data);
        deckManager.updateDeck(chosenPile);
        step++;
        log.info("Chosen pile: {}, Step: {}", chosenPile, step);

        if (step < 3) {
            messageSender.deleteMessages(chatId);
            performTrick();
        }
        else {
            messageSender.deleteMessages(chatId);
            showEleventhCard();
        }
    }

    private void showEleventhCard() {
        String cardToShow = deckManager.getSelectedCard();
        log.info("Card to show: {}", cardToShow);
        messageSender.addMessageId(
                messageSender.sendPhoto(
                        chatId,
                        cardToShow,
                        "Компания, которую вы загадали\uD83D\uDC46")
                        .getMessageId());
    }

}