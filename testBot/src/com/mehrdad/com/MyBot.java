package com.mehrdad.com;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Date;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class MyBot extends TelegramLongPollingBot {
	
	ControlTiming T1 = new ControlTiming();
	private static ArrayList<String> content = new ArrayList<String>();

	@Override
	public String getBotUsername() {
		
		return "mehrdadzareiabc";
	}

	@Override
	public void onUpdateReceived(Update update) {
		
		try {
			
			Message message = update.getMessage();

			if(message != null && message.hasText()){
				
				try {
					
					handleIncomingMessage(message);
				} catch (InvalidObjectException e) {
					
					e.printStackTrace();
				}				
			}			
		} catch (Exception e){
			
			e.printStackTrace();			
		}
	}

	private void handleIncomingMessage(Message message) throws InvalidObjectException {
		
		ControlTiming.timeTable.add(Integer.parseInt(message.getText()));
		ControlTiming.done.add(1);
		content.add(message.getText());
		ControlTiming.counter++;

		if ((ControlTiming.counter-1) == 0) T1.start();

		Date date = new Date();
		System.out.println(date.getTime()/1000 + "\tfrom computer");		
	}
	
	public void sendToTelegram(int i) {
		
		System.out.println(content.size());
		System.out.println(content.get(i));
		SendMessage messageSend = new SendMessage();
		
		messageSend.enableMarkdown(true);
		messageSend.setChatId("@testbotpy");
//		messageSend.setReplyToMessageId(message.getMessageId());
		messageSend.setText(content.get(i));
		
		try {
				
			sendMessage(messageSend);						
		} catch (TelegramApiException e) {
			
			e.printStackTrace();
		}
	}
	
	
	@Override
	public String getBotToken() {
		
		return "446550503:AAHnw-NNbxn1HTODReiZD4tWoR7piXxbRqM";
	}
}
