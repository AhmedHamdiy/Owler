import React, { useEffect, useState } from "react";
import {useNavigate} from 'react-router-dom';
//import axios from 'axios';

const SearchBar =(searchedQuery)=>{
    const navigate=useNavigate();
    const [query,setQuery]=useState('');
    const autoComplete =async(e)=>{
        setQuery(e.target.value);
        //Connect the prevoius queries database to suggest suitable query
    };
    
    const hootquery = async()=>{ 
        if (query==='') {
            return;
        }   
        try {
           //Precprocess the query??
    
           //Search for it in the database then navigate to the query page result with query-id page?  
            //If not Found in database navigate to NotFound Page
            
            //navigate(`/ResultsPage/${queryResult[0].query_id}`);
          } catch (error) {
            console.error('Search error:', error.response ? error.response.data : error.message);
            alert('failed hoot. Please check your query and try again.');
          }
        };
    return(
        <div>
            <input required type="search" placeholder="Type queries, then hoot!!" className="query-text-box" 
            value={query} onChange={autoComplete}/>
        <input type="submit" value="Hoot" className="hoot-button" onClick={hootquery}/>
        </div>
    );
};

export default SearchBar;