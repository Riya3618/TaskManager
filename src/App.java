import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.text.SimpleDateFormat;

public class App {
    
    // Task class that holds task details
    static class Task {
        String title;
        int priority; // Lower numbers are higher priority
        Date deadline;

        public Task(String title, int priority, Date deadline) {
            this.title = title;
            this.priority = priority;
            this.deadline = deadline;
        }

        public String toString() {
            return title + " (Priority: " + priority + ", Deadline: " + deadline + ")";
        }
    }

    // Priority Queue to hold tasks ordered by priority
    static class TaskManager {
        PriorityQueue<Task> taskQueue;

        public TaskManager() {
            taskQueue = new PriorityQueue<>(Comparator.comparingInt(task -> task.priority));
        }

        // Add task to queue
        public void addTask(Task task) {
            taskQueue.add(task);
        }

        // Remove task from queue
        public void removeTask(Task task) {
            taskQueue.remove(task);
        }

        // Get all tasks in priority order
        public ArrayList<Task> getAllTasks() {
            ArrayList<Task> tasks = new ArrayList<>(taskQueue);
            tasks.sort(Comparator.comparingInt(task -> task.priority)); // Sort by priority
            return tasks;
        }

        // Save tasks to a file (simple serialization)
        public void saveTasks() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
                oos.writeObject(taskQueue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load tasks from file
        @SuppressWarnings("unchecked")
        public void loadTasks() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
                taskQueue = (PriorityQueue<Task>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // Main Frame UI
    static class TaskManagerUI extends JFrame {
        private TaskManager taskManager;
        private DefaultListModel<String> listModel;
        private JList<String> taskList;

        public TaskManagerUI() {
            taskManager = new TaskManager();
            taskManager.loadTasks(); // Load tasks on start

            setTitle("Task Manager");
            setSize(400, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Task List
            listModel = new DefaultListModel<>();
            taskList = new JList<>(listModel);
            JScrollPane scrollPane = new JScrollPane(taskList);
            add(scrollPane, BorderLayout.CENTER);

            // Button Panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            JButton addButton = new JButton("Add Task");
            addButton.addActionListener(e -> openAddTaskDialog());

            JButton removeButton = new JButton("Remove Task");
            removeButton.addActionListener(e -> removeSelectedTask());

            JButton saveButton = new JButton("Save Tasks");
            saveButton.addActionListener(e -> taskManager.saveTasks());

            buttonPanel.add(addButton);
            buttonPanel.add(removeButton);
            buttonPanel.add(saveButton);
            add(buttonPanel, BorderLayout.SOUTH);

            // Timer to show reminders
            startReminderTimer();

            // Update task list view
            updateTaskList();
        }

        // Update the list view with current tasks
        private void updateTaskList() {
            listModel.clear();
            for (Task task : taskManager.getAllTasks()) {
                listModel.addElement(task.toString());
            }
        }

        // Open the Add Task Dialog
        private void openAddTaskDialog() {
            JTextField titleField = new JTextField(20);
            JTextField priorityField = new JTextField(5);
            JTextField deadlineField = new JTextField(20);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 2));
            panel.add(new JLabel("Task Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Priority (1 - High):"));
            panel.add(priorityField);
            panel.add(new JLabel("Deadline (YYYY-MM-DD):"));
            panel.add(deadlineField);

            int option = JOptionPane.showConfirmDialog(this, panel, "Add New Task", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                try {
                    String title = titleField.getText();
                    int priority = Integer.parseInt(priorityField.getText());
                    Date deadline = new SimpleDateFormat("yyyy-MM-dd").parse(deadlineField.getText());
                    Task newTask = new Task(title, priority, deadline);
                    taskManager.addTask(newTask);
                    updateTaskList(); // Update the task list after adding
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please try again.");
                }
            }
        }

        // Remove the selected task
        private void removeSelectedTask() {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                Task taskToRemove = taskManager.getAllTasks().get(selectedIndex);
                taskManager.removeTask(taskToRemove);
                updateTaskList(); // Update the task list after removal
            }
        }

        // Set up a reminder system
        private void startReminderTimer() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    checkForReminders();
                }
            }, 0, 60000); // Check every minute
        }

        // Check for tasks with upcoming deadlines and show reminders
        private void checkForReminders() {
            Date now = new Date();
            for (Task task : taskManager.getAllTasks()) {
                if (task.deadline != null && task.deadline.before(new Date(now.getTime() + 60 * 60 * 1000))) {
                    // Reminder if deadline is within the next hour
                    JOptionPane.showMessageDialog(this, "Reminder: " + task.title + " is due soon!");
                }
            }
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManagerUI ui = new TaskManagerUI();
            ui.setVisible(true);
        });
    }
}
