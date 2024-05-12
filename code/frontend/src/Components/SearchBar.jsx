import React, { useState } from "react";
import { useHistory } from 'react-router-dom';
import '../Styles/style.css'
import voiceSearchIcon from '../Styles/icons/voiceSearch.png';
import searchIcon from "../Styles/icons/search.png";

function SearchBar({ onSuggest }) {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestion, setShowSuggestion] = useState(false);
    const [selectedSuggestionIndex, setSelectedSuggestionIndex] = useState(-1);
    const history = useHistory();

    const hootQuery = (e) => {
        e.preventDefault();
        history.push(`/search?q=${query}`);
    };
    
    const handleKeyDown = (event) => {
        if (event.key === 'ArrowUp') {
            event.preventDefault();
            setSelectedSuggestionIndex((prevIndex) =>
                Math.max(prevIndex - 1, 0));
        } else if (event.key === 'ArrowDown') {
            event.preventDefault();
            setSelectedSuggestionIndex((prevIndex) =>
                Math.min(prevIndex + 1, suggestions.length - 1)
            );
        } else if (event.key === 'Enter') {
            if (selectedSuggestionIndex !== -1) {
                let newQuery = suggestions[selectedSuggestionIndex];
                setQuery(newQuery);
                setSuggestions([]);
                setShowSuggestion(false);
                setSelectedSuggestionIndex(-1);
            }
            else
                hootQuery(event);
        }
    };
    
    const getSuggestions = async (queryLength) => {
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
        onSuggest(newQuery.length);
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
                    placeholder="Type Queries Then Hoot"
                    onKeyDown={handleKeyDown}/>
                
                <img src={searchIcon} alt="Search"
                    onClick={hootQuery} className="icon"/>
                
                <img src={voiceSearchIcon} alt="Voice Search"
                    onClick={voiceSearch} className="icon"/>
            </div>
            
            {showSuggestion&& (
                <ul className="suggestions-list">
                    {suggestions.map((suggestion, index) => (
                        <li key={index}
                            className={index === selectedSuggestionIndex ? 'selected suggestion' : 'suggestion'}
                            onClick={() => handleSuggestionClick(suggestion)}>
                            {suggestion}
                        </li>))}
                </ul>
            )}
        </div>
    );
}

export default SearchBar;
