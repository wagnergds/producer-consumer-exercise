/*
 * (c) 2014 UL TS BV
 */
package com.ul;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Consumer {

    private BlockingQueue<Message> queue;
    private Thread consumerThread = null;

    public Consumer(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    public void startConsuming() {
        consumerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Message> listMsg = new ArrayList<>();
                List<Message> file = new ArrayList<>();

                int count = 0;
                while (true) {
                    try {
                        Message message = queue.take();
                        listMsg.add(message);
                        file.add(message);
                        count ++;
                        if (count >= 9){
                            Collections.sort(listMsg, new Comparator<Message>() {
                                @Override
                                public int compare(Message o1, Message o2) {
                                    if (o1.getPriority() == o2.getPriority()) {
                                        return o1.getText().compareTo(o2.getText());
                                    }
                                    else {
                                        return o1.getPriority().compareTo(o2.getPriority());
                                    }
                                }
                            });
                            listMsg.stream().forEach(l -> System.out.println( l.getPriority() + " - " + l.getTimestamp() + " - " + l.getText()));
                            count = 0;
                            listMsg = new ArrayList<>();
                        }

                      } catch (InterruptedException e) {
                        // executing thread has been interrupted, exit loop
                        break;
                    }
                }
                try {
                    BufferedWriter bwFile = new BufferedWriter(new FileWriter("messages.txt"));
                    StringBuffer strFile = new StringBuffer();
                    Collections.sort(file, new Comparator<Message>() {
                        @Override
                        public int compare(Message o1, Message o2) {
                            if (o1.getPriority() == o2.getPriority()) {
                                return o1.getText().compareTo(o2.getText());
                            }
                            else {
                                return o1.getPriority().compareTo(o2.getPriority());
                            }
                        }
                    });
                    file.stream().forEach(f -> {
                            strFile.append(f.getPriority() + " - " + f.getTimestamp() + " - " + f.getText());
                            strFile.append("\n");
                    });

                    bwFile.write(strFile.toString());
                    bwFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        consumerThread.start();
    }

    public void stopConsuming() {
        consumerThread.interrupt();
    }
}
