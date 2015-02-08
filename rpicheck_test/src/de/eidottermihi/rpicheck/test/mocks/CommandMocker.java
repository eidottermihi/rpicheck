package de.eidottermihi.rpicheck.test.mocks;

import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

/**
 * Mocker for {@link Command}
 * 
 * @author Michael
 *
 */
public class CommandMocker {

	private Session.Command command = Mockito.mock(Session.Command.class);

	public CommandMocker withResponse(String output) {
		Mockito.when(command.getInputStream()).thenReturn(
				IOUtils.toInputStream(output));
		return this;
	}

	public CommandMocker withExitStatus(int status) {
		Mockito.when(command.getExitStatus()).thenReturn(status);
		return this;
	}

	public Command mock() {
		return this.command;
	}

}
