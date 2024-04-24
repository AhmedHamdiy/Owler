import React, { useEffect, useState } from "react";
import voiceSearchIcon from '../Styles/voiceSearch.png';
import searchIcon from "../Styles/search.png";

function SearchBar(props) {
    const [query, setQuery] = useState('');

    const hootQuery = () => {
        console.log('Hoot button clicked');
        // props.history.push(`/search-results/${query}`);
        // NOT FINISHED
    };

    const makeSuggestions = (event) => {
        //make the backend call to get suggestions
        // NOT FINISHED
        const newQuery = event.target.value;
        console.log('Suggestions:', newQuery);
        setQuery(newQuery);
    };

    const voiceSearch = () => {
        // Implement voice search
        // Use the browser's speech recognition API ??
        // I Need more research on this
        console.log('Voice search clicked');
    };

    useEffect(() => {
        // Check if props.myQuery exists before setting the query
        if (props.myQuery && props.myQuery.query) {
            setQuery(props.myQuery.query);
        }
    }, [props.myQuery]);

    return (
        <div>
            <input
                required
                type="search"
                placeholder={query}
                style={styles.queryTextBox}
                onChange={makeSuggestions}
                value={query}
            />
            <img
                src={searchIcon}
                alt="Search"
                width={40}
                height={40}
                style={{ ...styles.icon, left: "90%" }}                
                onClick={hootQuery}
            />
            <img
                src={voiceSearchIcon}
                alt="Voice Search"
                width={40}
                height={40}
                style={{ ...styles.icon, left: "73%" }}
                onClick={voiceSearch}
            />
        </div>
    );
}

const styles = {
    queryTextBox: {
        margin: "0 auto",
        borderRadius: "25px",
        height: "52px",
        paddingLeft: "30px",
        paddingRight: "50px",
        fontSize: "larger",
        width: "100%"
    },
    icon: {
        width: "35px",
        height: "35px",
        cursor: "pointer",
        position: "relative",
        top: "-43px"      
    }
};

export default SearchBar;
