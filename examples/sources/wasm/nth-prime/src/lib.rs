#[no_mangle]
pub extern "C" fn foo(n: i32) -> i32 {
    let n = n + 1;
    let limit = sieve_upper_bound(n.try_into().unwrap());

    let mut sieve = vec![true; limit];

    sieve[0] = false;
    sieve[1] = false;

    let mut count = 0;

    for prime in 2..limit {
        if !sieve[prime] {
            continue;
        }

        count += 1;
        if count == n {
            return prime.try_into().unwrap();
        }

        for multiple in ((prime * prime)..limit).step_by(prime) {
            sieve[multiple] = false;
        }
    }

    2
}

fn sieve_upper_bound(n: u32) -> usize {
    let x = if n <= 10 { 10.0 } else { n as f64 };

    (x * (x * (x).ln()).ln()).ceil() as usize
}