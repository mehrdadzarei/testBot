package com.mehrdad.com;

import java.io.InvalidObjectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class MyBot extends TelegramLongPollingBot {
	
	ControlTiming T1 = new ControlTiming();
	private Connection con;
	private Statement st;
	private PreparedStatement prSt;
	private ResultSet rs;

	public MyBot() {

		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/testbot"
					+ "?autoReconnect=true&useSSL=false", "mehrdad-zarei", "mehr.4000");
			st = con.createStatement();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}		
		
	public void initial() {

		String query = "select time from test";
		
		try {
			
			rs = st.executeQuery(query);
			
			while (rs.next()) {
				
				ControlTiming.timeTable.add(rs.getInt("time"));
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			try {
				
				rs.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		ControlTiming.counter = ControlTiming.timeTable.size();
		T1.start();
		
		Date date = new Date();
		System.out.println(date.getTime()/1000 + "\tfrom computer");
	}
	
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
		ControlTiming.counter++;

		try {
			
			String query = "insert into test values (default, ?, ?, ?, ?)";
			prSt = con.prepareStatement(query);
			prSt.setLong(1, message.getChatId());							// chatId
			prSt.setString(2, message.getChat().getUserName());				// userName
			prSt.setString(3, message.getText());							// message
			prSt.setInt(4, Integer.parseInt(message.getText()));			// time
			prSt.executeUpdate();		
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			try {
				
				prSt.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
		Date date = new Date();
		System.out.println(date.getTime()/1000 + "\tfrom computer");
	}
	
	@SuppressWarnings("deprecation")
	public void sendToTelegram(int i) {
		
		SendMessage messageSend = new SendMessage();
		String querySel = "select * from test limit " + i +",1";
		String queryDel;
		
		try {
					
			rs = st.executeQuery(querySel);
			if (rs.next()) {
			
				int id;
				
				id = rs.getInt("Id");
				messageSend.setText(rs.getString("message"));

				queryDel = "delete from test where Id = " + id;
				prSt = con.prepareStatement(queryDel);
				prSt.execute();
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			try {
				
				rs.close();
				prSt.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		messageSend.enableMarkdown(true);
		messageSend.setChatId("@testbotpy");
//		messageSend.setReplyToMessageId(message.getMessageId());		
		
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
