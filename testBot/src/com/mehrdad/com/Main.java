package com.mehrdad.com;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {

	public static void main(String[] args) {
		
		ApiContextInitializer.init();
		
		TelegramBotsApi botsApi = new TelegramBotsApi();
		
		try {
			botsApi.registerBot(new MyBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
