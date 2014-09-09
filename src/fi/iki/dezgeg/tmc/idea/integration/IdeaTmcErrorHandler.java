package fi.iki.dezgeg.tmc.idea.integration;

import com.intellij.openapi.ui.Messages;
import fi.helsinki.cs.tmc.core.TMCErrorHandler;
import fi.helsinki.cs.tmc.core.ui.UserVisibleException;

public class IdeaTmcErrorHandler implements TMCErrorHandler {
    @Override
    public void raise(String s) {
        handleException(new UserVisibleException(s));
    }

    @Override
    public void handleException(Exception e) {
        Messages.showErrorDialog(e.getMessage(), "IdeaTmcErrorHandler.handleException()");
    }

    @Override
    public void handleManualException(String s) {
        Messages.showErrorDialog(s, "IdeaTmcErrorHandler.handleManualException()");
    }
}
