package com.vexsoftware.votifier.hook;

import java.security.KeyPair;

import com.ulfric.lib.api.hook.VotifierHook.IVotifier;
import com.vexsoftware.votifier.Votifier;

public enum VotifierImpl implements IVotifier {

	INSTANCE;

	@Override
	public KeyPair getKeyPair()
	{
		return Votifier.getInstance().getKeyPair();
	}

	@Override
	public void setKeyPair(KeyPair keypair)
	{
		Votifier.getInstance().setKeyPair(keypair);
	}

	@Override
	public boolean isDebug()
	{
		return Votifier.getInstance().isDebug();
	}

}