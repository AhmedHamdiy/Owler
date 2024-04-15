import React, { useState ,useEffect} from "react";
import { useNavigate } from 'react-router-dom';
import  '../Styles/App.css';
//import axios from 'axios';
import Owl from '../Styles/Owl.png'
import SearchResult from "../Components/SearchResult";
import SearchBar from "../Components/SearchBar";

const ResultsPage =({searchedQuery,query_id}) => {
    const [query,setQuery]=useState(''); 
    const [results, setResults] = useState([]);
    const [searchTime, setSearchTime] = useState('');
    const navigate=useNavigate();
    //Fetch results from the database and set the Search Time
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