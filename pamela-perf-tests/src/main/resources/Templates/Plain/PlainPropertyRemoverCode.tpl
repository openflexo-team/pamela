	public void {$removerName}({$typeAsString} {$name}) {
		this.{$internalVariableName}.remove({$name});
		getPropertyChangeSupport().firePropertyChange("{$name}", {$name}, null);
	}

