//***********************************************
// Michael Meluso
// CSC 470 - CLoud Computing
// Project 3: AWS EC2 and SES
// 
// SESClient class
// Runs sample code to test functionality
// of AWS Java API calls to SES
//**********************************************

package src.com.melusom2.sp3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.DeleteVerifiedEmailAddressRequest;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import com.amazonaws.services.simpleemail.model.GetSendStatisticsResult;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;

public class SESClient {

    static final String FROM = "email@example.com";  // This address must be verified.
    static final String SUBJECT = "Hello from SES";

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

    private static boolean isEmail(String query) {
        Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher m = p.matcher(query);
        return m.matches();
    }

    public static void run() {
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

        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SES");
        System.out.println("===========================================\n");

        // Instantiate an Amazon SES client, which will make the service call with the supplied AWS credentials.
        AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);

        // Main program execution
        try {
            // Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your production
            // access status, sending limits, and Amazon SES identity-related settings are specific to a given
            // AWS region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using
            // the US East (N. Virginia) region. Examples of other regions that Amazon SES supports are US_WEST_2
            // and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
            Region REGION = Region.getRegion(Regions.US_EAST_1);
            client.setRegion(REGION);

            // List verified email addresses
            System.out.println("Verified email addresses with this account:");
            ListVerifiedEmailAddressesResult verResult = client.listVerifiedEmailAddresses();
            System.out.println(verResult.toString());
            pause();

            // Verify a new email address
            String newEmail = "";
            while (!isEmail(newEmail)) {
                System.out.println("Enter a new email address to verify in a valid format (john@example.com):");
                newEmail = new java.util.Scanner(System.in).nextLine();
            }
            client.verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(newEmail));
            pause();

            // Send a new email
            System.out.println("Creating a new email...");
            String dest = "";
            while (!isEmail(dest)) {
                System.out.println("Enter the destination email address in a valid format (john@example.com):");
                dest = new java.util.Scanner(System.in).nextLine();
            }

            System.out.println("Would you like to add CC email addresses? (Y/N):");
            String answer = new java.util.Scanner(System.in).next();
            Collection<String> cc = new LinkedList<>();
            while(answer.charAt(0)=='Y' || answer.charAt(0)=='y') {
                System.out.println("Enter a CC email address in a valid format (john@example.com):");
                String newCC = new java.util.Scanner(System.in).nextLine();
                if (isEmail(newCC)) {
                    cc.add(newCC);
                } else {
                    System.out.println("Email address not in a valid format.");
                }
                System.out.println("Would you like to add more CC addresses? (Y/N):");
                answer = new java.util.Scanner(System.in).nextLine();
            }

            System.out.println("Would you like to add BCC email addresses? (Y/N):");
            answer = new java.util.Scanner(System.in).next();
            Collection<String> bcc = new LinkedList<>();
            while(answer.charAt(0)=='Y' || answer.charAt(0)=='y') {
                System.out.println("Enter a BCC email address in a valid format (john@example.com):");
                String newBCC = new java.util.Scanner(System.in).nextLine();
                if (isEmail(newBCC)) {
                    bcc.add(newBCC);
                } else {
                    System.out.println("Email address not in a valid format.");
                }
                System.out.println("Would you like to add more BCC addresses? (Y/N):");
                answer = new java.util.Scanner(System.in).next();
            }

            // Construct an object to contain the recipient address, CC and BCC.
            Destination destination = new Destination()
                .withToAddresses(dest)
                .withCcAddresses(cc)
                .withBccAddresses(bcc);

            // Create the subject and body of the message.
            Content subject = new Content().withData(SUBJECT);
            Content textBody = new Content().withData("Greetings, " + dest + ", from Amazon SES!");
            Body body = new Body().withText(textBody);

            // Create a message with the specified subject and body.
            Message message = new Message().withSubject(subject).withBody(body);

            // Assemble the email.
            SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);

            // Send the email.
            client.sendEmail(request);
            System.out.println("Email sent!");
            pause();

            // Get send quota and statistics
            System.out.println("Send quota:");
            System.out.println((client.getSendQuota()).toString());
            System.out.println("Send statistics:");
            System.out.println((client.getSendStatistics()).toString());
            pause();

            // Delete a verified email address
            System.out.println("Type a verified email address to delete:");

            String delEmail = "";
            while (!isEmail(delEmail)) {
                System.out.println("Enter a verified email address to delete in a valid format (john@example.com):");
                delEmail = new java.util.Scanner(System.in).nextLine();
            }
            client.deleteVerifiedEmailAddress(new DeleteVerifiedEmailAddressRequest().withEmailAddress(delEmail));
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
        System.out.println("SES program terminating");
        System.out.println("===========================================\n");
    }
}
