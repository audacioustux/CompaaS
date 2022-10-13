<script lang="ts">
	import { ws } from '$lib/websocketConnection';
	import { writable } from 'svelte/store';
	type State = {
		requests: Array<Request>;
	};
	// Create a new store with the given data.
	export const state = writable<State>({
		requests: []
	});
	export const connect = () => {
		ws.addEventListener('open', (event: any) => {
			ws.send('world');
			ws.send('audacioustux');
		});
		ws.addEventListener('message', (message: any) => {
			const data: Request = message.data;
			state.update((state) => ({
				...state,
				requests: [data].concat(state.requests)
			}));
		});
	};
    connect()
</script>

<h1>Welcome to SvelteKit</h1>
<p>Visit <a href="https://kit.svelte.dev">kit.svelte.dev</a> to read the documentation</p>

{#each $state.requests as request}
	<div>
		{request}
	</div>
{/each}