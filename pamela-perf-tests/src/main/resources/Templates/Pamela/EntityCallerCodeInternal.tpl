		for (int i = 0; i < 100; i++) {
			{$childEntity.name} child = {$childEntity.name}();
			returned.{$childEntityProperty.adderName}(child);
			child.{$childEntity.parentEntityProperty.setterName}(returned);
		}