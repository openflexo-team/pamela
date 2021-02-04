	public void {$adderName}({$typeAsString} {$name}) {
		this.{$internalVariableName}.add({$name});
		getPropertyChangeSupport().firePropertyChange("{$name}", null, {$name});
	}

