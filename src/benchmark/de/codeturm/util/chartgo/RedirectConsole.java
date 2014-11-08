package de.codeturm.util.chartgo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class RedirectConsole {

  @SuppressWarnings("serial")
  public class CapturePane extends JPanel implements Consumer {

    private JTextArea output;

    public CapturePane() {
      setLayout(new BorderLayout());
      Font font = new Font("Courier New", Font.PLAIN, 12);
      output = new JTextArea();
      output.setFont(font);
      output.setForeground(Color.ORANGE);
      output.setBackground(Color.DARK_GRAY);
      add(new JScrollPane(output));
    }

    @Override
    public void appendText(final String text) {
      if (EventQueue.isDispatchThread()) {
        output.append(text);
        output.setCaretPosition(output.getText().length());
      } else {

        EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
            appendText(text);
          }
        });

      }
    }
  }

  public interface Consumer {
    void appendText(String text);
  }

  public class StreamCapturer extends OutputStream {

    private StringBuilder buffer;
    private Consumer consumer;
    private PrintStream old;
    private String prefix;

    public StreamCapturer(String prefix, Consumer consumer, PrintStream old) {
      this.prefix = prefix;
      buffer = new StringBuilder(128);
      if (prefix != null && !prefix.isEmpty())
        buffer.append("[").append(prefix).append("] ");
      this.old = old;
      this.consumer = consumer;
    }

    @Override
    public void write(int b) throws IOException {
      char c = (char) b;
      String value = Character.toString(c);
      buffer.append(value);
      if (value.equals("\n")) {
        consumer.appendText(buffer.toString());
        buffer.delete(0, buffer.length());
        if (prefix != null && !prefix.isEmpty())
          buffer.append("[").append(prefix).append("] ");
      }
      if (old != null)
        old.print(c);
    }
  }

  public static void main(String[] args) {
    System.out.println("Hello, this is a test");
    new RedirectConsole("RedirectConsole");
    System.out.println("Wave if you can see me");
  }

  public RedirectConsole(String title) {

    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CapturePane capturePane = new CapturePane();
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(capturePane);
        frame.setSize(Double.valueOf(screenSize.getWidth() * 0.8).intValue(), 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        PrintStream ps = System.out;
        System.setOut(new PrintStream(new StreamCapturer("", capturePane, ps))); // "STDOUT"

        synchronized (RedirectConsole.this) {
          RedirectConsole.this.notifyAll();
        }
      }
    });
  }
}