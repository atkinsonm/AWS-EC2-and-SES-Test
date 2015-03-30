//***********************************************
// Michael Meluso
// CSC 470 - CLoud Computing
// Project 3: AWS EC2 and SES
// 
// Driver class
// Driver for the AWS EC2 and SES Client
//**********************************************

package src.com.melusom2.sp3;

public class Driver {
	public static void main(String argv[]) throws java.lang.IllegalThreadStateException {
		SESClient ses = new SESClient();
		ses.run();
		EC2Client ec2 = new EC2Client();
		ec2.run();
		System.exit(0);
	}
}
