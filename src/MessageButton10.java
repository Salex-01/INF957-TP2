import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButton10 extends MessageButton {
    public MessageButton10(Container c1, Main4 m) {
        super(c1, m, 3);
        setLabel("10 aléatoires");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        sendMessages(10, -1, -1);
    }
}