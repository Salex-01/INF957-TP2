import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButtonContinuous extends GraphButton {
    public MessageButtonContinuous(Container c1, Main4 m) {
        super(c1, m, 7);
        setBackground(Color.RED);
        setForeground(Color.WHITE);
        setLabel("Envoi en continu");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (m4.g.toggleContinuous()) {
            setBackground(Color.GREEN);
            setForeground(Color.BLACK);
        } else {
            setBackground(Color.RED);
            setForeground(Color.WHITE);
        }
    }
}