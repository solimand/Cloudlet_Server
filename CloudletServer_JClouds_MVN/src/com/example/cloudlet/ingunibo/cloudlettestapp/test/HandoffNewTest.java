package com.example.cloudlet.ingunibo.cloudlettestapp.test;

import com.example.cloudlet.ingunibo.cloudlettestapp.Handoff_NEW_Thread;

public class HandoffNewTest {

	public static void main(String[] args) {
		System.out.println("\nThis simulation starts after the client connects to new cloudlet and request for an handoff");
		//give the next thread the IP of previous cloudlet station, got via phone...
		Handoff_NEW_Thread ho_thread_test = new Handoff_NEW_Thread("192.168.1.11");
		ho_thread_test.start();//connecting prev cloudlet station
		try {
			ho_thread_test.join();//Handoff to me
		} catch (InterruptedException e) {
			System.out.println("\nInterruption during handoff request");
			e.printStackTrace();
		}
		System.out.println("\nSynth for Handoff starts, wait.");
	}

}
