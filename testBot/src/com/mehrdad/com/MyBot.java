package com.mehrdad.com;

import java.io.InvalidObjectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiParser.EmojiTransformer;

public class MyBot extends TelegramLongPollingBot {
	
	ControlTiming T1 = new ControlTiming();
	private Connection con;
	private Statement st;
	private PreparedStatement prSt;
	private ResultSet rs;
	private static Vector<String> recMessage = new Vector<String>(100,2);
	private static Vector<String> recTime = new Vector<String>(100,2);
	// Julian time for 1970/01/01 is 1348/10/11
	private static final int y0J = 1348;
	private static final int m0J = 10;
	private static final int d0J = 11;
	private static final int h0 = 0;
	private static final int m0 = 0;
	private static final int hpy = 8766;	// One Julian year has 8766 hours: 1 year = (365.25 days) × (24 hours/day) = 8766 hours
	private static final int second0 = (((y0J-1)*hpy+(6*31+(m0J-6-1)*30)*24+(d0J-1)*24+h0)*60+m0)*60;
	
	public MyBot() {

		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/testbot"
					+ "?autoReconnect=true&useSSL=false&ampuseUnicode=true&characterEncoding=utf-8", "mehrdad-zarei", "mehr.4000");
			st = con.createStatement();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}		
		
	public void initial() {

		String query = "select time from information";
		
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
	}
	
	@Override
	public String getBotUsername() {
		
		return "mehrdadzareiabc";
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		
		try {
			
			Message message = update.getMessage();
			SendMessage sendMessg = new SendMessage();

			if(message != null && message.hasText()){
				
				String messgText = message.getText();
				String mesgtime = "";
				long idChat = message.getChatId();

				if (messgText.length() >= 5) mesgtime = messgText.subSequence(0, 5).toString();

				if (messgText.equals("/start")) {
					
					ReplyKeyboardMarkup keyMarkup = new ReplyKeyboardMarkup();
					List<KeyboardRow> key = new ArrayList<>();
					KeyboardRow row = new KeyboardRow();
					String firstName = message.getChat().getFirstName();
					String text = EmojiParser.parseToUnicode(
								  "Hi " + firstName + " :smile:" +
								  "\nWellcom to Alarm Bot" +
								  "\nat first enter your message, that you want to show you" +
								  "\nthen enter your time, when you want to get alarm" +
								  "\nin the final press record button" +
								  "\nhave a nice days");

					row.add("message");
					row.add("time");
					key.add(row);
						
					row = new KeyboardRow();
					row.add("record");
					key.add(row);
						
					keyMarkup.setKeyboard(key);
					sendMessg.setReplyMarkup(keyMarkup);
					
					sendIncomingMessage(sendMessg, idChat, text);
					
					try {
						
						String query = "insert into initialInformation values (default, ?, ?)";
						
						prSt = con.prepareStatement(query);
						prSt.setLong(1, idChat);										// chatId
						prSt.setString(2, message.getChat().getUserName());				// userName
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
				} else if (messgText.equals("message")) {
						
					String text = "type your message like the example:" +
								  "\n/message your message";
						
					sendIncomingMessage(sendMessg, idChat, text);
					
				} else if (messgText.equals("time")) {
						
					String text = "type your time like year-month-day-hour-minute" +
								  "\nexample:" +
								  "\n/time 1396-05-09-15-05";

					sendIncomingMessage(sendMessg, idChat, text);
					
				} else if (mesgtime.equals("/mess")) {
						
					String text = "now press the time button";
					String textMess;
					
					try {
						
						textMess = idChat + messgText.substring(9).toString();
					} catch (Exception e) {
						
						textMess = "you recorded an alarm for this time";
					}
						
					for (int i = 0; i < recMessage.size(); i++)						
						
						if (recMessage.get(i).subSequence(0, message.getChatId().toString().length()).equals(message.getChatId().toString())) {
							
							recMessage.remove(i);
							break;
						}

					recMessage.add(textMess);
					sendIncomingMessage(sendMessg, idChat, text);
					
				} else if (mesgtime.equals("/time")) {
						
					String text = "now press the record button";
					String textTime;
					
					try {
						
						textTime = idChat + messgText.substring(6).toString();

						for (int i = 0; i < recTime.size(); i++)						
							
							if (recTime.get(i).subSequence(0, message.getChatId().toString().length()).equals(message.getChatId().toString())) {
								
								recTime.remove(i);
								break;
							}

						recTime.add(textTime);
						sendIncomingMessage(sendMessg, idChat, text);
						
					} catch (Exception e) {
						
						String textError = "your time is incorrect" +
							  	  		   "\nplease enter your time like this:" +
							  	           "\n/time 1396-05-09-15-05";
			
						sendIncomingMessage(sendMessg, idChat, textError);
					}
				} else if (messgText.equals("record")) {
										
					try {
							
						recordMessage(sendMessg, idChat,message);
					} catch (InvalidObjectException e) {
							
						e.printStackTrace();
					}					
				} else {
					
					String text = "your message is incorrect" +
								  "\nplease chose one of the buttons";
					
					sendIncomingMessage(sendMessg, idChat, text);
				}
			}			
		} catch (Exception e){
			
			e.printStackTrace();			
		}
	}

	private void recordMessage(SendMessage sendMessg, long idChat, Message message) throws InvalidObjectException {
		
		String messText = "", timeText = "";
		Date date = new Date();;
		int yJ, mJ, dJ, h, m;
		int second,elapsedTime;
		
		for (int i = 0; i < recMessage.size(); i++)
			
			if (recMessage.get(i).subSequence(0, message.getChatId().toString().length()).equals(message.getChatId().toString()))
				
				messText = recMessage.get(i).substring(message.getChatId().toString().length());
		
		if (messText.isEmpty()) messText = "you recorded an alarm for this time";
		
		for (int i = 0; i < recTime.size(); i++)
			
			if (recTime.get(i).subSequence(0, message.getChatId().toString().length()).equals(message.getChatId().toString()))
				
				timeText = recTime.get(i).substring(message.getChatId().toString().length());
		
		try {
		
			yJ = Integer.parseInt(timeText.subSequence(0, 4).toString());
			mJ = Integer.parseInt(timeText.subSequence(5, 7).toString());
			dJ = Integer.parseInt(timeText.subSequence(8, 10).toString());
			h = Integer.parseInt(timeText.subSequence(11, 13).toString());
			m = Integer.parseInt(timeText.subSequence(14, 16).toString());		
		
			mJ = mJ>6 ? (6*31+(mJ-6-1)*30):(mJ-1)*31;
			second = (((yJ-1)*hpy+mJ*24+(dJ-1)*24+h)*60+m)*60;
			elapsedTime = second-second0-16200;			// 16200 second is different time.
			
			if (elapsedTime <= date.getTime()/1000) {
			
				String text = "your time is expired" +
					  	  	  "\nplease enter another time";
		
				sendIncomingMessage(sendMessg, idChat, text);
			} else {
				
				ControlTiming.timeTable.add(elapsedTime);
				ControlTiming.counter++;
				
				String textRec = "your alarm has been recorded";					
				sendIncomingMessage(sendMessg, idChat, textRec);			

				try {
			
					String query = "insert into information values (default, ?, ?, ?)";
					prSt = con.prepareStatement(query);
					prSt.setLong(1, idChat);										// chatId
					prSt.setString(2, messText);									// message
					prSt.setInt(3, elapsedTime);									// time
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
			}
		} catch (Exception e) {
			
			String text = "your time is incorrect" +
					  	  "\nplease enter your time like this:" +
					  	  "\n/time 1396-05-09-15-05";
		
			sendIncomingMessage(sendMessg, idChat, text); 
		}
	}
	
	@SuppressWarnings("deprecation")
	private void sendIncomingMessage(SendMessage sendMessg, long idChat, String text) {
		
		sendMessg.setChatId(idChat);
		sendMessg.setText(text);
		try {
			
			sendMessage(sendMessg);
		} catch (TelegramApiException e) {
			
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void sendToTelegram(int i) {
		
		SendMessage messageSend = new SendMessage();
		String querySel = "select * from information limit " + i +",1";
		String queryDel;
		
		try {
					
			rs = st.executeQuery(querySel);
			if (rs.next()) {
			
				int id;
				
				id = rs.getInt("Id");
				messageSend.setText(rs.getString("message"));
				messageSend.setChatId(rs.getLong("chatId"));

				try {
					
					sendMessage(messageSend);						
				} catch (TelegramApiException e) {
					
					e.printStackTrace();
				}
				
				queryDel = "delete from information where Id = " + id;
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
	}	
		
	@Override
	public String getBotToken() {
		
		return "446550503:AAHnw-NNbxn1HTODReiZD4tWoR7piXxbRqM";
	}
}
