use unidecode::unidecode;

#[no_mangle]
pub extern "C" fn slugify(max_length: i32) -> i32 {
    let string = "Nín hǎo. Wǒ shì zhōng guó rén";
    let stop_words = "";
    let sep = "-";
    let max_length = max_length as usize;

    let char_vec: Vec<char> = sep.chars().collect();
    let mut string: String = unidecode(string.into())
        .to_lowercase()
        .trim()
        .trim_matches(char_vec[0])
        .replace(' ', &sep.to_string());

    for word in stop_words.split(",") {
        if !word.is_empty() {
            string = string.replace(word, &sep.to_string());
        }
    }

    let mut slug = Vec::with_capacity(string.len());

    let mut is_sep = true;

    for x in string.chars() {
        match x {
            'a'..='z' | '0'..='9' => {
                is_sep = false;
                slug.push(x as u8);
            }
            _ => {
                if !is_sep {
                    is_sep = true;
                    slug.push(char_vec[0] as u8);
                } else {
                }
            }
        }
    }

    if slug.last() == Some(&(char_vec[0] as u8)) {
        slug.pop();
    }

    let mut s = String::from_utf8(slug).unwrap();

    s.truncate(max_length);
    s = s.trim_end_matches(char_vec[0]).to_string();

    s.as_bytes().iter().fold(0, |acc, &x| acc + x as i32)
}
