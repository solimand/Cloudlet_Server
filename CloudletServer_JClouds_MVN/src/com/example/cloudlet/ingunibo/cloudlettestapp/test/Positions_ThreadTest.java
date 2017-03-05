package com.example.cloudlet.ingunibo.cloudlettestapp.test;

import com.example.cloudlet.ingunibo.cloudlettestapp.Positions_Thread;

public class Positions_ThreadTest {

	public static void main(String[] args) {
		System.out.println("Simulation: receiving positions...");
		
		Positions_Thread posTrd_test = new Positions_Thread();
		posTrd_test.start();		
		try{
			posTrd_test.join();
		}
		catch (InterruptedException e) {
			System.out.println("\nInterruption during the positions reception ");
			e.printStackTrace();
		}
		System.out.println("\nPositions received. DONE.");
	}

}
