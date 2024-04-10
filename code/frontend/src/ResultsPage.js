import React, { useState ,useEffect} from "react";
import { useNavigate } from 'react-router-dom';
import './App.css';
import axios from 'axios';
import Owl from './Owl.png'
import SearchResult from "./SearchResult";
import SearchBar from "./SearchBar";

const ResultsPage =({searchedQuery,query_id}) => {
    const [query,setQuery]=useState('');
    const [results, setResults] = useState([]);
    const [searchTime, setSearchTime] = useState('');
    const navigate=useNavigate();
    const fetchResults = async () => {
        try {
          const response = await axios.get('http://localhost:3001/fetchResults');
          console.log(response.data);
          return response.data;
        } catch (error) {
          console.error('Error fetching results:', error.message);
          throw error;
        }
      };
    
      useEffect(() => {
        let isMounted = true;
    
        const getFeedgetResultsbacks = async () => {
        try {
            const getResultsFromDB = await fetchResults();
        if (isMounted) {
            setResults(getResultsFromDB);
        }
        } catch (error) {
            console.log(error);
        }
        };
        getFeedgetResultsbacks();
        return () => {
        isMounted = false;
        };
    }, []);
    
    return (
        <div className="serach-bar-container">
            <img src={Owl} alt="Owl" width={30} height={30}/>
            <SearchBar searchedQuery={searchedQuery}/>
            {
                results.map((result,index)=>{
                    <SearchResult key={index} title={result.title} URL={result.URL} snippet={result.snippet}/>
                })
            }
            {/*
            pagination
            */}
        </div>
    );

}

export default ResultsPage;