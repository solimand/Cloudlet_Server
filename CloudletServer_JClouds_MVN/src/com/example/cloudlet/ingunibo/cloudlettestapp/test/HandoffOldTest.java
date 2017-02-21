package com.example.cloudlet.ingunibo.cloudlettestapp.test;

import com.example.cloudlet.ingunibo.cloudlettestapp.Handoff_OLD_Thread;

public class HandoffOldTest {

	public static void main(String[] args) {
		System.out.println("\nThis simulation starts when a cloudlet contact previouis station and ask for handoff");
		Handoff_OLD_Thread ho_old_thread_test = new Handoff_OLD_Thread();
		ho_old_thread_test.start();//wait for handoff req
		try {
			ho_old_thread_test.join();//Handoff to me
		} catch (InterruptedException e) {
			System.out.println("\nInterruption during handoff request");
			e.printStackTrace();
		}
		System.out.println("\nHandoff sent. DONE.");
	}

}
