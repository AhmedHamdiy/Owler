import React, { useState } from "react";
import { useHistory } from 'react-router-dom';
import '../Styles/style.css'
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
            const requestOptions = {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain' 
                },
                body: query 
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
        <div className="search-bar-container">
            <div className="search-bar">
                <input required type="search" onChange={makeSuggestions}
                    className="query-text-box" value={query}
                    placeholder="Type Queries Then Hoot" />
                
                <img src={searchIcon} alt="Search"
                    onClick={hootQuery} className="icon"/>
                
                <img src={voiceSearchIcon} alt="Voice Search"
                    onClick={voiceSearch} className="icon"/>
            </div>
            
            {showSuggestion&& (
                <ul className="suggestions-list">
                    {suggestions.map((suggestion, index) => (
                        <li key={index} className="suggestion"
                        onClick={() => handleSuggestionClick(suggestion)}>{suggestion}</li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default SearchBar;
