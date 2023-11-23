package org.toolup.network.ssh.bean;

import java.util.ArrayList;
import java.util.List;

public class ShellCommand {
	
	private boolean sudo;
	
	private String command;
	
	private List<String> args = new ArrayList<String>();

	public ShellCommand command(String command) {
		this.command = command;
		return this;
	}
	
	public ShellCommand arg(String arg) {
		args.add(arg);
		return this;
	}

	public ShellCommand withSudo() {
		this.sudo = true;
		return this;
	}
	
	public boolean isSudo() {
		return sudo;
	}

	public String getCommand() {
		return command;
	}

	public List<String> getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return String.format("%s%s %s", this.isSudo() ? "sudo " : "", 
				this.getCommand(),
				String.join(" ", this.getArgs()));
	}
	

	public static ShellCommand chmod() { return new ShellCommand().command("chmod"); }
	public static ShellCommand chown() { return new ShellCommand().command("chown"); }
	public static ShellCommand mv() { return new ShellCommand().command("mv"); }
	public static ShellCommand cp() { return new ShellCommand().command("cp"); }
	public static ShellCommand rm() { return new ShellCommand().command("rm"); }
	public static ShellCommand mktemp() { return new ShellCommand().command("mktemp"); }
}
