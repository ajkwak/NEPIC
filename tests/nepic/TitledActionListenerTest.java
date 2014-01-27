package nepic;

import static org.junit.Assert.assertEquals;

import java.awt.event.ActionEvent;

import org.junit.Test;

/**
 * JUnit tests for {@link TitledActionListener}.
 *
 * @author AJ Parmidge
 */
public class TitledActionListenerTest {

    @Test(expected = NullPointerException.class)
    public void ctor_nullText_throws() {
        new TestTitledActionListener((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctor_emptyText_throws() {
        new TestTitledActionListener("");
    }

    @Test
    public void ctor_nonEmptyText() {
        String buttonText = "Hello, world!";
        TitledActionListener buttonHandler = new TestTitledActionListener(buttonText);
        assertEquals(buttonText, buttonHandler.getText());
    }

    /**
     * A test implementation of the abstract {@link TitledActionListener} class.
     * 
     * @author AJ Parmidge
     */
    private class TestTitledActionListener extends TitledActionListener {

        public TestTitledActionListener(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing here.
        }

    }

}
