package org.mifosplatform.payments.evo.api;

/**
 * @author  Christoph C.M. M�hring
 * @version 1.0
 */
public abstract class Coder
{
	abstract String encode(String text);
	abstract String decode(String text);
}
