	public void {$setterName}({$typeAsString} {$name}) {
		if ({$name} != this.{$internalVariableName}) {
			{$typeAsString} oldValue = this.{$internalVariableName};
			this.{$internalVariableName} = {$name};
			getPropertyChangeSupport().firePropertyChange("{$name}", oldValue, {$name});
		}
	}

