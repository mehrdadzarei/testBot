package com.mehrdad.com;

import java.util.Date;
import java.util.Vector;

public class ControlTiming extends Thread {

	private Thread t = new Thread();	
	public static Vector<Integer> timeTable = new Vector<Integer>(100,2);
	public static int counter = 0;
	Date date;
	MyBot bot;	
	
	@Override
	public void run() {
		
		try {
			
			date = new Date();				
			while (true) {
				
				date = new Date();		
				for (int i = 0; i < counter; i++)
					if (timeTable.get(i) == date.getTime()/1000){
						
						bot.sendToTelegram(i);
						timeTable.remove(i);
						counter--;
					}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void start() {
		
		bot = new MyBot();
		t = new Thread (this);
		t.start();
	}
}
