import React, { Fragment, useState } from "react";
import { useHistory } from 'react-router-dom';
import voiceSearchIcon from '../Styles/voiceSearch.png';
import searchIcon from "../Styles/search.png";

function SearchBar({ onSuggest }) {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestion, setShowSuggestion] = useState(false);
    const history = useHistory();

    const hootQuery = (e) => {
        e.preventDefault();
        history.push(`/search?q=${query}`);
    };

    const getSuggestions = async (query) => {
        try {
            //const response = await fetch(`http://localhost:5000/suggest/${query}`);
            const requestOptions = {
                method: 'POST',
                headers: {
                  'Content-Type': 'text/plain' // Specify the content type as plain text
                },
                body: query // Set the plain text data as the body of the request
            };

            const response = await fetch("http://localhost:5000/suggest", requestOptions);

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            console.log(data);
            return data;
        } catch (error) {
            console.error('Error fetching suggestions:', error);
            return [];
        }
    };

    const makeSuggestions = (e) => {
        const newQuery = e.target.value;
        setQuery(newQuery);
        onSuggest(newQuery);
        if (newQuery.length === 0) {
            setSuggestions([]);
            setShowSuggestion(false);
        } else {
            getSuggestions(newQuery).then((suggestions) => {
                setSuggestions(suggestions);
                setShowSuggestion(true);
            });
        }
    };

    const handleSuggestionClick = (suggestion) => {
        setQuery(suggestion);
        setSuggestions([]);
        setShowSuggestion(false);
        onSuggest(suggestion);
    };

    const voiceSearch = () => {
        const recognition = new window.webkitSpeechRecognition();
        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            setQuery(transcript);
            onSuggest({ target: { value: transcript } });
            makeSuggestions({ target: { value: transcript } });
        };
        recognition.start();
    };

    return (
        <Fragment>
            <div style={styles.searchBar}>
                <input required type="search" onChange={makeSuggestions}
                    style={styles.queryTextBox} value={query} placeholder="Search"/>
                
                <img src={searchIcon} alt="Search"
                    onClick={hootQuery} style={styles.icon}/>
                
                <img src={voiceSearchIcon} alt="Voice Search"
                    onClick={voiceSearch} style={styles.icon}/>
                
            </div>
            
            {showSuggestion&& (
                <ul style={styles.suggestionsList}>
                    {suggestions.map((suggestion, index) => (
                        <li key={index} style={styles.suggestion}
                        onClick={() => handleSuggestionClick(suggestion)}>{suggestion}</li>
                    ))}
                </ul>
            )}
        </Fragment>
    );
}

const styles = {
    searchBar: {
        display: "flex",
        flexDirection: "row",
        justifyContent: "center",
        alignItems: "center",
        position: "relative",
        top: "20px",
        backgroundColor: "white",
        borderRadius: "10px",
        height: "64px",
        padding: "0 20px",
        gap: "10px",
        width: "40%",
        marginBottom: "10px",
    },
    queryTextBox: {
        margin: "0 auto",
        height: "40px",
        fontSize: "larger",
        width: "100%",
        border: "none",
        outline: "none",
    },
    icon: {
        margin: "10 auto",
        width: "35px",
        height: "35px",
        cursor: "pointer",
    },
    suggestionsList: {
        listStyleType: "none",
        padding: "10px 0",
        margin: -14.8,
        backgroundColor: "#fff",
        boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
        borderRadius: "10px",
        width: "42.2%",
        zIndex: 1,
        fontSize: "larger",
    },
    suggestion:{
        padding: "10px",
        marginTop: "5px",
        cursor: "pointer",
    },
};

export default SearchBar;
