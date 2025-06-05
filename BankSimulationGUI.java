//These are the necessary imports for creating the GUI (javax.awt.* and java.swing.*) and handling queues (java.util.LinkedList and java.util.Queue).

import java.awt.*;
import javax.swing.*;
import java.util.LinkedList;
import java.util.Queue;

// Customer Class
// This class represents a customer with arrivalTime and serviceTime.
class Customer {
    int arrivalTime; // When the customer arrives.
    int serviceTime; // How long the customer needs to be served.

    // Constructor: Initializes the Customer object with arrival and service times.
    Customer(int arrivalTime, int serviceTime) {
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
    }
}

// Counter Class
// This class represents a service counter.
class Counter {
    Queue<Customer> queue = new LinkedList<>(); // Queue to hold customers.
    int endTime = 0; // Time when the counter finishes serving all customers.
    int servedCount = 0; // Number of customers served.
    int totalServiceTime = 0; // Total service time of all served customers.

    // Adds a customer to the queue.
    void addCustomer(Customer customer) {
        queue.add(customer);
    }

    // Serves a customer, returns the wait time.
    int serveCustomer() {
        if (queue.isEmpty()) {
            return 0;
        }
        Customer customer = queue.poll();
        int waitTime = Math.max(0, endTime - customer.arrivalTime);
        endTime = Math.max(endTime, customer.arrivalTime) + customer.serviceTime;
        totalServiceTime += customer.serviceTime;
        servedCount++;
        return waitTime;
    }

    // Checks if the counter is idle.
    boolean isIdle() {
        return queue.isEmpty();
    }

    // Getter methods for respective fields.
    int getEndTime() {
        return endTime;
    }

    int getQueueSize() {
        return queue.size();
    }

    int getServedCount() {
        return servedCount;
    }

    int getTotalServiceTime() {
        return totalServiceTime;
    }
}

// BankSimulationGUI Class
// This class extends JFrame and represents the main GUI for the simulation. 
public class BankSimulationGUI extends JFrame {

    private Counter[] counters; // Array of counters
    private int totalWaitTime = 0; // Total wait time
    private int numberOfCustomers; // Number of customers
    private int serviceTime; // Service time per customer
    private int arrivalRate; // Arrival rate of customers

    // Constructor: Initializes the GUI components and sets up the layout.
    public BankSimulationGUI() {
        setTitle("Bank Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        // Starts the input collection process.
        collectInputs();
    }

    // Collects user inputs, simulates customer arrivals, processes the customers, and displays results.
    private void collectInputs() {
        SwingUtilities.invokeLater(() -> {
            try {
                getUserInput(); // Collect user inputs
                simulateCustomerArrival(); // Simulate customer arrival
                processCustomers(); // Process customers
                displayResults(); // Display results
            } catch (NumberFormatException e) {
                // Handle invalid input errors
                JOptionPane.showMessageDialog(this, "Invalid input! Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                collectInputs(); // Restart input collection on error
            }
        });
    }

    // Collects inputs for the number of counters, customers, service time, and arrival rate.
    private void getUserInput() {
        int numberOfCounters = getIntInput("Enter number of service counters:");
        if (numberOfCounters <= 0) {
            throw new NumberFormatException("Number of service counters must be positive.");
        }
        counters = new Counter[numberOfCounters];
        for (int i = 0; i < numberOfCounters; i++) {
            counters[i] = new Counter();
        }

        numberOfCustomers = getIntInput("Enter number of customers:");
        if (numberOfCustomers <= 0) {
            throw new NumberFormatException("Number of customers must be positive.");
        }
        serviceTime = getIntInput("Enter time each customer will take at the counter:");
        if (serviceTime <= 0) {
            throw new NumberFormatException("Service time must be positive.");
        }
        arrivalRate = getIntInput("Enter number of customers coming to the bank every 2 units of time:");
        if (arrivalRate <= 0) {
            throw new NumberFormatException("Arrival rate must be positive.");
        }
    }

    // Displays a dialog to collect user input and validates that it is a number.
    private int getIntInput(String message) {
        String input;
        while (true) {
            input = JOptionPane.showInputDialog(this, message);
            if (input == null) {
                System.exit(0); // Exit if user cancels the input dialog
            }
            try {
                int value = Integer.parseInt(input.trim());
                if (value <= 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a positive number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Simulates the arrival of customers.
    private void simulateCustomerArrival() {
        int currentTime = 0;
        for (int i = 0; i < numberOfCustomers; i++) {
            Customer customer = new Customer(currentTime, serviceTime);
            int minQueueIndex = 0;
            for (int j = 1; j < counters.length; j++) {
                if (counters[j].getQueueSize() < counters[minQueueIndex].getQueueSize()) {
                    minQueueIndex = j;
                }
            }
            counters[minQueueIndex].addCustomer(customer);

            if ((i + 1) % arrivalRate == 0) {
                currentTime += 2;
            }
        }
    }

    // Processes all customers in the queues of each counter.
    private void processCustomers() {
        totalWaitTime = 0;
        for (Counter counter : counters) {
            while (!counter.isIdle()) {
                totalWaitTime += counter.serveCustomer();
            }
        }
    }

    // Calculates and displays the results of the simulation.
    private void displayResults() {
        int totalTime = 0;
        for (Counter counter : counters) {
            totalTime = Math.max(totalTime, counter.getEndTime());
        }

        double averageWaitingTime = (double) totalWaitTime / numberOfCustomers;

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("<html><body><h2>Simulation Inputs</h2>");
        resultBuilder.append("<ul>");
        resultBuilder.append("<li>Number of service counters: ").append(counters.length).append("</li>");
        resultBuilder.append("<li>Number of customers: ").append(numberOfCustomers).append("</li>");
        resultBuilder.append("<li>Service time per customer: ").append(serviceTime).append(" units</li>");
        resultBuilder.append("<li>Arrival rate (customers per 2 units of time): ").append(arrivalRate).append("</li>");
        resultBuilder.append("</ul>");

        resultBuilder.append("<h2>Simulation Results</h2>");
        resultBuilder.append("<ul>");
        resultBuilder.append("<li>Total time taken: ").append(totalTime).append(" units</li>");
        for (int i = 0; i < counters.length; i++) {
            resultBuilder.append("<li>Counter #").append(i + 1).append(":<ul>");
            resultBuilder.append("<li>Number of customers served: ").append(counters[i].getServedCount()).append("</li>");
            resultBuilder.append("<li>Total service time: ").append(counters[i].getTotalServiceTime()).append(" units</li>");
            resultBuilder.append("<li>End time: ").append(counters[i].getEndTime()).append(" units</li>");
            resultBuilder.append("</ul></li>");
        }
        resultBuilder.append("</ul>");

        resultBuilder.append("<h2>Overall Statistics</h2>");
        resultBuilder.append("<ul>");
        resultBuilder.append("<li>Total wait time: ").append(totalWaitTime).append(" units</li>");
        resultBuilder.append("<li>Average waiting time per customer: ").append(String.format("%.2f", averageWaitingTime)).append(" units</li>");
        resultBuilder.append("</ul></body></html>");

        JTextPane resultPane = new JTextPane();
        resultPane.setContentType("text/html");
        resultPane.setText(resultBuilder.toString());
        resultPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(resultPane);
        scrollPane.setPreferredSize(new Dimension(600, 400)); // Initial preferred size

        // Display results in a resizable dialog
        JDialog dialog = new JDialog(this, "Simulation Results", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().add(scrollPane);
        dialog.setSize(700, 500); // Sets an initial size for the dialog
        dialog.setResizable(true); // Allows the dialog to be resizable
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Prompt to run another simulation after closing the results dialog
        int option = JOptionPane.showConfirmDialog(this, "Would you like to run another simulation?", "Simulation Complete", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            collectInputs();
        } else {
            System.exit(0); // Close the application if the user chooses "No"
        }
    }

    // Main Method: Entry point of the application. 
    // It creates and runs an instance of the ‘BankSimulationGUI’ class on the event dispatch thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankSimulationGUI::new);
    }
}
