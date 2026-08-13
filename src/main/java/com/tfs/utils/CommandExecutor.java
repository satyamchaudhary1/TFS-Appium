package com.tfs.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class CommandExecutor {

    private CommandExecutor() {
    }

    public static CommandResult execute(String... command) {

        List<String> outputLines = new ArrayList<>();

        int exitCode;

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream()
                                 )
                         )) {

                String line;

                while ((line = reader.readLine()) != null) {

                    outputLines.add(line);

                    System.out.println(line);
                }
            }

            exitCode = process.waitFor();

        } catch (Exception e) {

            exitCode = -1;

            outputLines.add(
                    "Command execution failed: "
                            + e.getMessage()
            );
        }

        return new CommandResult(
                exitCode,
                String.join(
                        System.lineSeparator(),
                        outputLines
                )
        );
    }


    public static class CommandResult {

        private final int exitCode;
        private final String output;

        public CommandResult(
                int exitCode,
                String output
        ) {

            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}