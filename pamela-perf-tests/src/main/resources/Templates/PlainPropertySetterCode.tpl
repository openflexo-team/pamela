	public void {$setterName}({$typeAsString} {$name}) {
		if (({$name} == null && this.{$internalVariableName} != null) || ({$name} != null && !{$name}.equals(this.{$internalVariableName}))) {
			{$typeAsString} oldValue = this.{$internalVariableName};
			this.{$internalVariableName} = {$name};
			getPropertyChangeSupport().firePropertyChange("{$name}", oldValue, {$name});
		}
	}

