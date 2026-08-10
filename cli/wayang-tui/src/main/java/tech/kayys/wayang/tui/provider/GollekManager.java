package tech.kayys.wayang.tui.provider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class GollekManager {

    private static final int GOLLEK_PORT = 9131;

    private static String gollekPath = "gollek";

    public static boolean isInstalled() {
        try {
            Process process = new ProcessBuilder("which", "gollek").start();
            if (process.waitFor() == 0) {
                return true;
            }
            
            // Fallback 1: ~/.local/bin/gollek
            java.io.File localInstall = new java.io.File(System.getProperty("user.home"), ".local/bin/gollek");
            if (localInstall.exists() && localInstall.canExecute()) {
                gollekPath = localInstall.getAbsolutePath();
                return true;
            }

            // Fallback 2: ~/.gollek/bin/gollek
            java.io.File defaultInstall = new java.io.File(System.getProperty("user.home"), ".gollek/bin/gollek");
            if (defaultInstall.exists() && defaultInstall.canExecute()) {
                gollekPath = defaultInstall.getAbsolutePath();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRunning() {
        try (Socket socket = new Socket("127.0.0.1", GOLLEK_PORT)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void install() {
        System.out.println("Installing Gollek via script...");
        try {
            Process process = new ProcessBuilder("bash", "-c", "curl -sSL https://raw.githubusercontent.com/kayys-tech/gollek/main/scripts/install.sh | bash")
                    .inheritIO()
                    .start();
            process.waitFor();
            System.out.println("Gollek installation complete.");
        } catch (Exception e) {
            System.err.println("Failed to install Gollek: " + e.getMessage());
        }
    }

    public static void start() {
        System.out.println("Starting Gollek server (" + gollekPath + ")...");
        try {
            // Start gollek serve as a detached process
            new ProcessBuilder("bash", "-c", "nohup " + gollekPath + " serve > /dev/null 2>&1 &")
                    .start();
            
            // Wait up to 5 seconds for it to start
            for (int i = 0; i < 10; i++) {
                if (isRunning()) {
                    System.out.println("Gollek is now running!");
                    return;
                }
                Thread.sleep(500);
            }
            System.err.println("Gollek started, but port " + GOLLEK_PORT + " is still not responding.");
        } catch (Exception e) {
            System.err.println("Failed to start Gollek: " + e.getMessage());
        }
    }

    public static void ensureGollekRunning() {
        Scanner scanner = new Scanner(System.in);

        if (!isInstalled()) {
            System.out.print("Gollek not exist, did you want me to download? or by yourself? (auto/manual): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if ("auto".equals(answer)) {
                install();
                start();
            } else {
                System.out.println("Please install Gollek manually and start it with 'gollek serve'.");
                System.exit(1);
            }
        } else if (!isRunning()) {
            System.out.print("Gollek are not running, did you want wayang to run gollek? (Y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.isEmpty() || answer.equals("y") || answer.equals("yes")) {
                start();
            }
        }
    }
}
