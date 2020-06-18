package fr.sdis64.ui

// Workaround for youtrack.jetbrains.com/issue/KTOR-539
fun monkeyPatchFetchForCredentials() {
    js(
        """
        window.originalFetch = window.fetch;
        window.fetch = function (resource, init) {
            init = Object.assign({}, init);
            init.credentials = init.credentials !== undefined ? init.credentials : 'include';
            return window.originalFetch(resource, init);
        };
    """
    )
}
