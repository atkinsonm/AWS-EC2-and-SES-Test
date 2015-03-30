//***********************************************
// Michael Meluso
// CSC 470 - CLoud Computing
// Project 3: AWS EC2 and SES
// 
// EC2Client class
// Runs sample code to test functionality
// of AWS Java API calls to EC2
//**********************************************

package src.com.melusom2.sp3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class EC2Client {

    static final String UBUNTU_STD = "ami-84562dec";

    private AmazonEC2         ec2;
    private ArrayList<String> instanceIds;
    private ArrayList<String> spotInstanceRequestIds;
    /**
     * Pauses the program; waits for user input to continue
     */
    private static void pause() {
        System.out.println("Press Any Key To Continue...");
          new java.util.Scanner(System.in).nextLine();
    }

    /**
     * Overloaded pause method that supplies custom message
     */
    private static void pause(String message) {
        System.out.println(message);
          new java.util.Scanner(System.in).nextLine();
    }

    public void run() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            // Change the config file to another file
            credentials = new ProfilesConfigFile("profiles.txt").getCredentials("default");
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location, and is in valid format.",
                    e);
        }

        ec2 = new AmazonEC2Client(credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        ec2.setRegion(usEast1);

        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon EC2");
        System.out.println("===========================================\n");

        try {
            // Create EC2 Instance
            System.out.println("Creating t1.micro Ubuntu instance...");
            List<String> groupName = new ArrayList<String>();
            groupName.add("CSC470");//wait to out our instance into this group

            int minInstanceCount = 1; // create 1 instance
            int maxInstanceCount = 1;
            RunInstancesRequest rir = new RunInstancesRequest(UBUNTU_STD, minInstanceCount, maxInstanceCount);
            rir.setInstanceType("t1.micro");
            rir.setKeyName("CSC470");// give the instance the key we just created
            rir.setSecurityGroups(groupName);// set the instance in the group we just created
          
            RunInstancesResult result = ec2.runInstances(rir);
            System.out.println("Instance successfully launched!");
            pause();

            System.out.println("Enter the instance ID to fetch information about that instance:");
            String query = new java.util.Scanner(System.in).nextLine();
            List<String> descName = new ArrayList<String>();
            descName.add(query);//wait to out our instance into this group
            DescribeInstancesResult desc = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(descName));
            System.out.println("Query returned:\n" + desc.toString());
            pause();

            System.out.println("Enter an instance ID to terminate that instance:");
            String term = new java.util.Scanner(System.in).nextLine();
            List<String> termName = new ArrayList<String>();
            termName.add(term);//wait to out our instance into this group
            TerminateInstancesResult termRes = ec2.terminateInstances(new TerminateInstancesRequest(termName));
            System.out.println("Instance terminated!");
            pause();

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (Exception ex) {
            System.out.println("Something went wrong.");
            System.out.println("Error message: " + ex.getMessage());
        }

        System.out.println("===========================================");
        System.out.println("EC2 program terminating");
        System.out.println("===========================================\n");
    }
}
