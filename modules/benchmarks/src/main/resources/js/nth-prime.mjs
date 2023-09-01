export function nth_prime(n) {
    n = n + 1;
    let limit = sieve_upper_bound(n);

    // Sieve of Eratosthenes: https://en.wikipedia.org/wiki/Sieve_of_Eratostthenes
    let sieve = Array(limit).fill(true);

    sieve[0] = false;
    sieve[1] = false;

    let count = 0;

    for (let prime = 2; prime < limit; prime++) {
        // continue if not prime
        if (!sieve[prime]) {
            continue;
        }

        // terminate on nth prime
        count += 1;
        if (count == n) {
            return prime;
        }

        // false-ify all multiples of prime
        for (let multiple = prime * prime; multiple < limit; multiple += prime) {
            sieve[multiple] = false;
        }
    }

    return 2;
}

function sieve_upper_bound(n) {
    let x = n <= 10 ? 10 : n;

    return Math.ceil(x * Math.log(x * Math.log(x)));
}