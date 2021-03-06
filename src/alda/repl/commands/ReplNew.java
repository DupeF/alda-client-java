
package alda.repl.commands;

import java.util.Arrays;
import java.io.IOException;

import alda.AldaServer;
import alda.Util;
import alda.AldaResponse.AldaScore;
import java.util.function.Consumer;
import jline.console.ConsoleReader;

/**
 * Handles the :new command
 * Simply deletes the contents of the stringbuffer passed into it
 */
public class ReplNew implements ReplCommand {
  private ReplCommandManager cmdManager;

  public ReplNew(ReplCommandManager m) {
    cmdManager = m;
  }

  @Override
  public void act(String args, StringBuffer history, AldaServer server,
                  ConsoleReader reader, Consumer<AldaScore> newInstrument) {

    if (history.length() > 0) {
      // Verify we can delete score
      if (!Util.promptWithChoices(reader, ReplLoad.OVERWRITE_WARNING,
                                          "yes", "no").equals("yes")) {
        return;
      }
    }

    // Clear history
    history.delete(0, history.length());
    // Clear prompt
    newInstrument.accept(null);
    // Clear last save file (so we don't overwrite things)
    cmdManager.setSaveFile(null);
  }
  @Override
  public String docSummary() {
    return "Creates a new score.";
  }
  @Override
  public String key() {
    return "new";
  }
}
