package modules;

import java.util.*;
import javax.persistence.*;

public class DataErrorException extends Exception
{
	public DataErrorException(String message) {
		super(message);
	}
}
