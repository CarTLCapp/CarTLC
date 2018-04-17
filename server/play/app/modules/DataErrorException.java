/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import java.util.*;
import javax.persistence.*;

public class DataErrorException extends Exception
{
	public DataErrorException(String message) {
		super(message);
	}
}
