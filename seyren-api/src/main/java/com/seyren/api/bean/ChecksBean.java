/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.api.bean;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.seyren.api.jaxrs.ChecksResource;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;

@Named
public class ChecksBean implements ChecksResource {

	private ChecksStore checksStore;

	@Inject
	public ChecksBean(ChecksStore checksStore) {
		this.checksStore = checksStore;
	}
	
	@Override
	public Response getChecks(String states, Boolean enabled) {
		List<Check> checks;
		if (states != null) {
			checks = checksStore.getChecksByState(getStates(states));
		} else {
			checks = checksStore.getChecks();
		}
		if (enabled != null) {
			filterByEnabled(checks, enabled);
		}
		return Response.ok(checks).build();
	}

	@Override
	public Response createCheck(Check check) {
		if (check.getState() == null) {
			check.setState(AlertType.OK);
		}
		Check stored = checksStore.createCheck(check);
		return Response.created(uri(stored.getId())).build();
	}
	
	@Override
	public Response updateCheck(String checkId, Check check) {
		Check stored = checksStore.getCheck(checkId);
		if (stored == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		stored = checksStore.saveCheck(check);
		return Response.ok(stored).build();
	}
	
	@Override
	public Response getCheck(String checkId) {
		Check check = checksStore.getCheck(checkId);
		if (check == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(check).build();
	}
	
	@Override
	public Response deleteCheck(String checkId) {
		checksStore.deleteCheck(checkId);
		return Response.noContent().build();
	}
	
	private void filterByEnabled(final List<Check> checks, final boolean enabled) {
		CollectionUtils.filter(checks, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Check)object).isEnabled() == enabled;
			}
		});
	}
	
	private Set<String> getStates(String states) {
		Set<String> result = new HashSet<String>();
		for (String state : states.split(",")) {
			result.add(state.toUpperCase());
		}
		return result;
	}

	private URI uri(String checkId) {
		try {
			return new URI("checks/" + checkId);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
