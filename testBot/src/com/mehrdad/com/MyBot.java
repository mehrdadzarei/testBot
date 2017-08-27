package com.mehrdad.com;

import java.io.InvalidObjectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.vdurmont.emoji.EmojiParser;

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
				String mesgtime = "";					// for checking incoming message 
				String text = "";						// for send message to telegram inline
				String textMess;						// for saving message to database
				String textTime;						// for saving time to database
				int lenChatId = message.getChatId().toString().length();
				long idChat = message.getChatId();
				
				if (messgText.length() > 4) mesgtime = messgText.subSequence(messgText.length()-4, messgText.length()).toString();
				for (int i = 0; i < recMessage.size(); i++)						
					
					if (recMessage.get(i).subSequence(0, lenChatId).equals(message.getChatId().toString())) {
												
						switch (mesgtime) {
						
							case "پیام":
							
								recMessage.remove(i);
								break;
							case "زمان":
								
								if (recMessage.get(i).length() >= (lenChatId+2))
									
									if (recMessage.get(i).subSequence(lenChatId, lenChatId+2).equals("ok"))
										
										break;
								recMessage.remove(i);
								break;
							case "خیره":
								
								break;
							case "رگشت":
								
								if (recMessage.get(i).length() >= (lenChatId+2))
									
									if (recMessage.get(i).subSequence(lenChatId, lenChatId+2).equals("ok"))
										
										break;
								recMessage.remove(i);
								break;
							default :
								
								if (recMessage.get(i).length() >= (lenChatId+2))
									
									if (recMessage.get(i).subSequence(lenChatId, lenChatId+2).equals("ok"))
										
										break;
								recMessage.remove(i);
								mesgtime = "/mess";							
						}						
						break;
					}				
				for (int i = 0; i < recTime.size(); i++)						
					
					if (recTime.get(i).subSequence(0, lenChatId).equals(message.getChatId().toString())) {
																		
						switch (mesgtime) {
						
							case "پیام":
								
								if (recTime.get(i).length() >= (lenChatId+2))
									
									if (recTime.get(i).subSequence(lenChatId, lenChatId+2).equals("ok"))
										
										break;
								recTime.remove(i);
								break;
							case "زمان":
							
								recTime.remove(i);
								break;
							case "خیره":
								
								break;
							case "رگشت":
										
								recTime.remove(i);
								break;
							default :
								
								if (recTime.get(i).length() >= (lenChatId+2))
									
									if (recTime.get(i).subSequence(lenChatId, lenChatId+2).equals("ok"))
										
										break;
								recTime.remove(i);
								mesgtime = "/time";							
						}						
						break;
					}
				if (messgText.equals("/start")) {
															
					String firstName = message.getChat().getFirstName();
					text = EmojiParser.parseToUnicode(
								  "سلام " + firstName + " :blush:" +
								  "\nخوش اومدی به بات تلگرام یادآور" + " :rose:" +
								  "\nدر این بات شما می توانید قرار ملاقات ها، تولد دوستان و ... را ثبت کنید و در زمانی که می خواهید به شما اطلاع می دهم" +
								  "\nروز خوبی داشته باشین" +
								  "\n");
													
					createInitialMarkup(sendMessg, idChat, text);					
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
				} else if (mesgtime.equals("زودن")) {
						
					text = EmojiParser.parseToUnicode("برای ثبت پیام جدید از گزینه های زیر استفاده نمائید :point_down: ");
					
					createAddingMarkup(sendMessg, idChat, text);										
				} else if (mesgtime.equals("ه ها")) {
					
					recorded(sendMessg, idChat);
				} else if (mesgtime.equals("هنما")) {
					
					text = "راهنما";
					
					sendIncomingMessage(sendMessg, idChat, text);				
				} else if (mesgtime.equals("پیام")) {
					
					text = "لطفا پیام مورد نظر خود را بنویسید و ارسال کنید\n";
														
					textMess = idChat + "";
					recMessage.add(textMess);
					sendIncomingMessage(sendMessg, idChat, text);				
				} else if (mesgtime.equals("زمان")) {
						
					text = "لطفا زمان مورد نظر خود را مانند نمونه ارسال نمائید: \n\n"
							+ "1396/06/04-15:04\n";

					textTime = idChat + "";
					recTime.add(textTime);					
					sendIncomingMessage(sendMessg, idChat, text);					
				} else if (mesgtime.equals("/mess")) {
						
					text = "پیام شما ثبت شد";
										
					textMess = idChat + "ok" + messgText.toString();
					if (messgText.toString() == "") textMess = idChat + "ok" + "شما یک یادآور برای این زمان ثبت کرده بودید";
						
					recMessage.add(textMess);
					sendIncomingMessage(sendMessg, idChat, text);					
				} else if (mesgtime.equals("/time")) {
						
					text = "زمان شما ثبت شد";
										
					textTime = idChat + "ok" + messgText.toString();

					if (messgText.toString() == "")
						
						text = "زمان شما نادرست است \n"
								+ "لطفا مانند نمونه زمان را ارسال نمائید: \n\n"
								+ "1396/06/04-15:04\n";
					else
						
						recTime.add(textTime);
					sendIncomingMessage(sendMessg, idChat, text);
				} else if (mesgtime.equals("خیره")) {
										
					try {
							
						recordMessage(sendMessg, idChat,message);
					} catch (InvalidObjectException e) {
							
						e.printStackTrace();
					}					
				} else if (mesgtime.equals("رگشت")) {
					
					text = "رویدادهای خود را ثبت کنید";
					ReplyKeyboardRemove removeKeyMarkup = new ReplyKeyboardRemove();
					
					sendMessg.setReplyMarkup(removeKeyMarkup);
					createInitialMarkup(sendMessg, idChat, text);				
				} else if (mesgtime.equals(" تکی")) {
					
					text = "لطفا آیدی پیام مورد نظر را مانند نمونه زیر ارسال نمائید \n"
							+ "id=5\n";
										
					sendIncomingMessage(sendMessg, idChat, text);					
				} else if (messgText.substring(0, 3).equals("id=")) {
										
					String querySel = "select * from information";
					String queryDel = "delete from information where Id = " + messgText.substring(3);
					boolean del = false;
					int cnt = 0;
					
					try {
						
						rs = st.executeQuery(querySel);
						while (rs.next()) {
							
							if (rs.getInt("chatId") == idChat)
								
								if (rs.getInt("Id") == Integer.parseInt(messgText.substring(3))) {
									
									prSt = con.prepareStatement(queryDel);
									prSt.execute();
									del = true;
									ControlTiming.counter--;
									Thread.sleep(500);
									ControlTiming.timeTable.remove(cnt);									
									break;
								}
							cnt++;
						}
					} catch (Exception e) {
						
						e.printStackTrace();
					} finally {
						
						try {
							
							rs.close();	
							prSt.close();
						} catch (Exception e) {
							
						}
					}
					text = del ? "پیام شما حذف شد" : "پیام شما یافت نشد";
					sendIncomingMessage(sendMessg, idChat, text);					
				} else if (mesgtime.equals(" همه")) {
					
					text = "آیا می خواهید همه یادآورهای ذخیره شده را حذف کنید؟ \n";
					InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
	                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
	                List<InlineKeyboardButton> rowInline = new ArrayList<>();
	                
	                rowInline.add(new InlineKeyboardButton().setText("خیر").setCallbackData("delMessageNo"));
	                rowInline.add(new InlineKeyboardButton().setText("بله").setCallbackData("delMessageYes"));
	                // Set the keyboard to the markup
	                rowsInline.add(rowInline);
	                // Add it to the message
	                markupInline.setKeyboard(rowsInline);
	                sendMessg.setReplyMarkup(markupInline);
	                
	                sendIncomingMessage(sendMessg, idChat, text);					
				} else {
					
					text = EmojiParser.parseToUnicode("پیام شما نادرست است \n"
							+ "لطفا از گزینه های زیر انتخاب نمائید  :point_down: ");
										
					sendIncomingMessage(sendMessg, idChat, text);
				}
			} else if (update.hasCallbackQuery()) {
				
				callBackQuery(update, sendMessg);				
			}
		} catch (Exception e){
			
			e.printStackTrace();			
		}
	}
		
	private void createInitialMarkup(SendMessage sendMessg, long idChat, String text) {
		
		ReplyKeyboardMarkup keyMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> key = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		
		String textMarkup = EmojiParser.parseToUnicode(" :information_source: راهنما");
		row.add(textMarkup);
		textMarkup = EmojiParser.parseToUnicode(" :bookmark_tabs: ثبت شده ها");
		row.add(textMarkup);
		textMarkup = EmojiParser.parseToUnicode(" :heavy_plus_sign: افزودن");
		row.add(textMarkup);
		key.add(row);
									
		keyMarkup.setKeyboard(key);
		keyMarkup.setResizeKeyboard(true);
		sendMessg.setReplyMarkup(keyMarkup);	
		
		sendIncomingMessage(sendMessg, idChat, text);
	}

	private void createAddingMarkup(SendMessage sendMessg, long idChat, String text) {
		
		ReplyKeyboardRemove removeKeyMarkup = new ReplyKeyboardRemove();
		ReplyKeyboardMarkup keyMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> key = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		
		String textMarkup = EmojiParser.parseToUnicode(" :alarm_clock: زمان");
		row.add(textMarkup);		
		textMarkup = EmojiParser.parseToUnicode(" :email: پیام");
		row.add(textMarkup);
		key.add(row);
		
		row = new KeyboardRow();
		
		textMarkup = EmojiParser.parseToUnicode(" :arrow_left: برگشت");
		row.add(textMarkup);
		textMarkup = EmojiParser.parseToUnicode(" :heavy_plus_sign: ذخیره");
		row.add(textMarkup);
		key.add(row);
									
		keyMarkup.setKeyboard(key);
		keyMarkup.setResizeKeyboard(true);
		
		sendMessg.setReplyMarkup(removeKeyMarkup);
		sendMessg.setReplyMarkup(keyMarkup);
		sendIncomingMessage(sendMessg, idChat, text);
	}

	private void recorded(SendMessage sendMessg, long idChat) {

		String message = "پیام های ثبت شده شما به شکل زیر می باشد \n\n";		
		String querySel = "select * from information";
		int cnt = 1;

		ReplyKeyboardRemove removeKeyMarkup = new ReplyKeyboardRemove();
		ReplyKeyboardMarkup keyMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> key = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		
		String textMarkup = EmojiParser.parseToUnicode(" :arrow_left: برگشت");
		row.add(textMarkup);				
		textMarkup = EmojiParser.parseToUnicode(" :red_circle: حذف همه");
		row.add(textMarkup);		
		textMarkup = EmojiParser.parseToUnicode(" :heavy_minus_sign: حذف تکی ");
		row.add(textMarkup);
		key.add(row);
															
		keyMarkup.setKeyboard(key);
		keyMarkup.setResizeKeyboard(true);
		
		sendMessg.setReplyMarkup(removeKeyMarkup);
		sendMessg.setReplyMarkup(keyMarkup);
		
		try {
			
			rs = st.executeQuery(querySel);
			while (rs.next()) {
				
				if (rs.getInt("chatId") == idChat) {
					
					message += cnt + ".\n پیام: \n" + rs.getString("message") + "\n زمان: \n"
							+ rs.getString("textTime") + "\n" + "id=" + rs.getInt("Id") + "\n\n";
					cnt++;
				}
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
		
		if (cnt == 1) message = "شما پیام ثبت شده ندارید \n"
				+ "برای ثبت پیام جدید به صفحه قبل برگشته و گزینه افزودن را انتخاب نمائید \n";
		sendIncomingMessage(sendMessg, idChat, message);
	}

	private void recordMessage(SendMessage sendMessg, long idChat, Message message) throws InvalidObjectException {
		
		String messText = "", timeText = "";
		String text = "";
		Date date = new Date();
		int yJ, mJ, dJ, h, m;
		int second,elapsedTime;
		int inTime = 0;					// for keep index of recTime
		int lenChatId = message.getChatId().toString().length();
		
		for (int i = 0; i < recMessage.size(); i++)
			
			if (recMessage.get(i).subSequence(0, lenChatId).equals(message.getChatId().toString())) {
				
				messText = recMessage.get(i).substring(lenChatId + 2);
				break;
			}				
		if (messText == "") messText = "شما یک یادآور برای این زمان ثبت کرده بودید";		
		for (int i = 0; i < recTime.size(); i++)
			
			if (recTime.get(i).subSequence(0, lenChatId).equals(message.getChatId().toString())) {
				
				inTime = i;
				timeText = recTime.get(i).subSequence(lenChatId+2, lenChatId+18).toString();
				break;
			}		
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
			
				text = "زمان شما گذشته است \n"
						+ "لطفا یک زمان برای آینده ارسال نمائید \n";
		
				sendIncomingMessage(sendMessg, idChat, text);
			} else {
				
				InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                
                rowInline.add(new InlineKeyboardButton().setText("خیر").setCallbackData("recordMessageNo"));
                rowInline.add(new InlineKeyboardButton().setText("بله").setCallbackData("recordMessageYes"));
                // Set the keyboard to the markup
                rowsInline.add(rowInline);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                sendMessg.setReplyMarkup(markupInline);
                
                text = "یادآور شما به شکل زیر می باشد: \n\n"
                		+ "پیام: \n" + messText
                		+ "\n\n"
                		+ "زمان: \n" + timeText
                		+ "\n\n"
                		+ "آیا مایل به ذخیره یادآور هستید؟ \n";
				                
                recTime.set(inTime, (idChat + "ok" + timeText + elapsedTime));
                sendIncomingMessage(sendMessg, idChat, text);                				
			}
		} catch (Exception e) {
			
			text = "زمان شما نادرست است \n"
					+ "لطفا مانند نمونه زمان را ارسال نمائید: \n\n"
					+ "1396/06/04-15:04\n";
		
			sendIncomingMessage(sendMessg, idChat, text); 
		}
	}
		
	@SuppressWarnings("deprecation")
	private void callBackQuery(Update update, SendMessage sendMessg) {
		
		String callData = update.getCallbackQuery().getData();
		long idChat = update.getCallbackQuery().getMessage().getChatId();
		long message_id = update.getCallbackQuery().getMessage().getMessageId();
		int lenChatId = update.getCallbackQuery().getMessage().getChatId().toString().length();		
		String messText = "", timeText = "", elapsedTimeText = "";
		String text = "", editText = "";
		int elapsedTime;
		boolean reg = true;
		
		if (callData.equals("recordMessageYes")) {
			
			for (int i = 0; i < recMessage.size(); i++)
				
				if (recMessage.get(i).subSequence(0, lenChatId).equals(update.getCallbackQuery().getMessage().getChatId().toString())) {
					
					messText = recMessage.get(i).substring(lenChatId + 2);
					break;
				}
			if (messText == "") messText = "شما یک یادآور برای این زمان ثبت کرده بودید";
			for (int i = 0; i < recTime.size(); i++)
				
				if (recTime.get(i).subSequence(0, lenChatId).equals(update.getCallbackQuery().getMessage().getChatId().toString())) {
												
					timeText = recTime.get(i).subSequence(lenChatId + 2, lenChatId+18).toString();
					elapsedTimeText = recTime.get(i).substring(lenChatId+18);
					break;
				}										
			try {
				
				elapsedTime = Integer.parseInt(elapsedTimeText);						
				String query = "insert into information values (default, ?, ?, ?, ?)";
				prSt = con.prepareStatement(query);
				prSt.setLong(1, idChat);										// chatId
				prSt.setString(2, messText);									// message
				prSt.setString(3, timeText);									// time text
				prSt.setInt(4, elapsedTime);									// time
				prSt.executeUpdate();
										
				ControlTiming.timeTable.add(elapsedTime);
				ControlTiming.counter++;							
			} catch (Exception e) {
		
				reg = false;
				e.printStackTrace();
			} finally {
		
				try {
			
					prSt.close();
				} catch (Exception e) {
			
					e.printStackTrace();
				}
			}
			
			text = reg ? "پیام شما ذخیره شد":"پیام شما ذخیره نشد \n"
					+ "لطفا پیامی مناسب ارسال نمائید";
			sendIncomingMessage(sendMessg, idChat, text);
			editText = "یادآور شما به شکل زیر می باشد: \n\n"
	        		+ "پیام: \n" + messText
	        		+ "\n\n"
	        		+ "زمان: \n" + timeText
	        		+ "\n";
		} else if (callData.equals("recordMessageNo")) {
			
			for (int i = 0; i < recMessage.size(); i++)
				
				if (recMessage.get(i).subSequence(0, lenChatId).equals(update.getCallbackQuery().getMessage().getChatId().toString())) {
					
					messText = recMessage.get(i).substring(lenChatId + 2);
					break;
				}
			if (messText == "") messText = "شما یک یادآور برای این زمان ثبت کرده بودید";
			for (int i = 0; i < recTime.size(); i++)
				
				if (recTime.get(i).subSequence(0, lenChatId).equals(update.getCallbackQuery().getMessage().getChatId().toString())) {
												
					timeText = recTime.get(i).subSequence(lenChatId, lenChatId+16).toString();
					break;
				}
			text = "پیام شما ذخیره نشد";
			sendIncomingMessage(sendMessg, idChat, text);
			editText = "یادآور شما به شکل زیر می باشد: \n\n"
	        		+ "پیام: \n" + messText
	        		+ "\n\n"
	        		+ "زمان: \n" + timeText
	        		+ "\n";
		} else if (callData.equals("delMessageYes")) {
						
			String querySel = "select * from information";
			String queryDel;
			int cnt = 0, ex = 1;
			
			try {
				
				rs = st.executeQuery(querySel);
				while (rs.next()) {
					
					if (rs.getInt("chatId") == idChat) {
						
						queryDel = "delete from information where Id = " + rs.getInt("Id");
						prSt = con.prepareStatement(queryDel);
						prSt.execute();
						ControlTiming.counter--;
						Thread.sleep(500);
						ControlTiming.timeTable.remove(cnt-ex);
						ex++;
						System.out.println(cnt);
						
					}
					cnt++;
				}
			} catch (Exception e) {
				
				reg = false;
				e.printStackTrace();
			} finally {
				
				try {
					
					rs.close();	
					prSt.close();
				} catch (Exception e) {
										
				}
			}
			editText = "آیا می خواهید همه یادآورهای ذخیره شده را حذف کنید؟ \n";
			text = reg ? "پیام های شما حذف شدند" : "پیام های شما حذف نشدند\n"
					+ "دوباره تلاش کنید";
			if (ex == 1) text = "هیچ پیامی یافت نشد";
			sendIncomingMessage(sendMessg, idChat, text);
			
//			for (int i = 0; i < ControlTiming.counter; i++)
				
		} else if (callData.equals("delMessageNo")) {
			
			editText = "آیا می خواهید همه یادآورهای ذخیره شده را حذف کنید؟ \n";
		}
		
		EditMessageText editMessg = new EditMessageText();
		
		editMessg.setChatId(idChat);
		editMessg.setMessageId((int)message_id);
		editMessg.setText(editText);
		try {
			
            editMessageText(editMessg); 
        } catch (TelegramApiException e) {
        	
            e.printStackTrace();
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
